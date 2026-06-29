package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountBusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.InsufficientBalanceException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;

@RequiredArgsConstructor
@Slf4j
public class ProcessTransactionUseCase {

    private final AccountRepositoryPort repository;
    private final AccountCachePort cache;
    private final MovementEventPort movementEventPort;
    private final TransactionEventPort transactionEventPort;
    private final CardEventPort cardEventPort;

    public Completable execute(Transaction tx) {

        log.info("[ACCOUNT-TX] Processing transaction. txId={}, type={}, amount={}",
                tx.id(), tx.type(), tx.amount());

        return process(tx)
                .andThen(transactionEventPort.publishTransactionCompleted(tx)
                        .doOnComplete(() ->
                                log.info("[ACCOUNT-TX] TransactionCompletedEvent published. txId={}", tx.id())
                        )
                )
                .onErrorResumeNext(error -> {

                    log.error("[ACCOUNT-TX] Transaction failed. txId={}, reason={}",
                            tx.id(), error.getMessage(), error);

                    return transactionEventPort.publishTransactionFailed(tx, error.getMessage())
                            .doOnComplete(() ->
                                    log.warn("[ACCOUNT-TX] TransactionFailedEvent published. txId={}", tx.id())
                            )
                            .andThen(Completable.error(error));
                });
    }

    private Completable process(Transaction tx) {
        return switch (tx.type()) {
            case DEPOSIT -> deposit(tx);
            case WITHDRAWAL -> withdraw(tx);
            case TRANSFER -> transfer(tx);
            case DEBIT_CARD_PAYMENT -> debitCardPayment(tx);
            default -> Completable.error(
                    new AccountBusinessException("Unsupported transaction type: " + tx.type())
            );
        };
    }

    // ---------------------------------------------------------
    // DEPOSIT
    // ---------------------------------------------------------
    private Completable deposit(Transaction tx) {

        log.info("[ACCOUNT-TX] Deposit. txId={}, targetAccountId={}, amount={}",
                tx.id(), tx.targetProductId(), tx.amount());

        return repository.findById(tx.targetProductId())
                .switchIfEmpty(Single.error(new AccountNotFoundException("Target account not found")))
                .flatMap(account ->
                        repository.save(account.withBalance(account.balance().add(tx.amount())))
                )
                .flatMap(saved ->
                        updateCache(saved)
                                .andThen(Single.just(saved))
                )
                .flatMapCompletable(saved ->
                        movementEventPort.registerMovement(tx, saved, "DEPOSIT")
                                .doOnComplete(() ->
                                        log.info("[ACCOUNT-TX] Deposit movement registered. txId={}, accountId={}",
                                                tx.id(), saved.id())
                                )
                );
    }

    // ---------------------------------------------------------
    // WITHDRAWAL
    // ---------------------------------------------------------
    private Completable withdraw(Transaction tx) {

        log.info("[ACCOUNT-TX] Withdrawal. txId={}, sourceAccountId={}, amount={}",
                tx.id(), tx.sourceProductId(), tx.amount());

        return repository.findById(tx.sourceProductId())
                .switchIfEmpty(Single.error(new AccountNotFoundException("Source account not found")))
                .flatMap(account -> {
                    if (account.balance().compareTo(tx.amount()) < 0) {
                        return Single.error(new InsufficientBalanceException("Insufficient balance"));
                    }
                    return repository.save(account.withBalance(account.balance().subtract(tx.amount())));
                })
                .flatMap(saved ->
                        updateCache(saved)
                                .andThen(Single.just(saved))
                )
                .flatMapCompletable(saved ->
                        movementEventPort.registerMovement(tx, saved, "WITHDRAWAL")
                                .doOnComplete(() ->
                                        log.info("[ACCOUNT-TX] Withdrawal movement registered. txId={}, accountId={}",
                                                tx.id(), saved.id())
                                )
                );
    }

    // ---------------------------------------------------------
    // TRANSFER (Atomic + Compensated)
    // ---------------------------------------------------------
    private Completable transfer(Transaction tx) {

        log.info("[ACCOUNT-TX] Transfer. txId={}, source={}, target={}, amount={}",
                tx.id(), tx.sourceProductId(), tx.targetProductId(), tx.amount());

        return Single.zip(
                        repository.findById(tx.sourceProductId())
                                .switchIfEmpty(Single.error(new AccountNotFoundException("Source account not found"))),
                        repository.findById(tx.targetProductId())
                                .switchIfEmpty(Single.error(new AccountNotFoundException("Target account not found"))),
                        AccountTransferPair::new
                )
                .flatMapCompletable(pair -> {

                    Account source = pair.source();
                    Account target = pair.target();

                    if (source.balance().compareTo(tx.amount()) < 0) {
                        return Completable.error(new InsufficientBalanceException("Insufficient balance"));
                    }

                    Account updatedSource = source.withBalance(source.balance().subtract(tx.amount()));
                    Account updatedTarget = target.withBalance(target.balance().add(tx.amount()));

                    return repository.save(updatedSource)
                            .flatMap(savedSource ->
                                    repository.save(updatedTarget)
                                            .map(savedTarget -> new AccountTransferPair(savedSource, savedTarget))
                            )
                            .flatMapCompletable(savedPair ->
                                    updateCache(savedPair.source())
                                            .andThen(updateCache(savedPair.target()))
                                            .andThen(
                                                    movementEventPort.registerMovement(tx, savedPair.source(), "TRANSFER_OUT")
                                                            .andThen(movementEventPort.registerMovement(tx, savedPair.target(), "TRANSFER_IN"))
                                                            .doOnComplete(() ->
                                                                    log.info("[ACCOUNT-TX] Transfer movements registered. txId={}", tx.id())
                                                            )
                                            )
                            )
                            .onErrorResumeNext(e -> {

                                log.error("[ACCOUNT-TX] Transfer failed. Rolling back. txId={}, reason={}",
                                        tx.id(), e.getMessage());

                                // COMPENSATION: restore original balances
                                return repository.save(source)
                                        .flatMap(savedSource ->
                                                repository.save(target)
                                                        .map(savedTarget -> new AccountTransferPair(savedSource, savedTarget))
                                        )
                                        .flatMapCompletable(savedPair ->
                                                updateCache(savedPair.source())
                                                        .andThen(updateCache(savedPair.target()))
                                                        .andThen(Completable.error(e))
                                        );
                            });
                });
    }

    // ---------------------------------------------------------
    // DEBIT CARD PAYMENT
    // ---------------------------------------------------------
    private Completable debitCardPayment(Transaction tx) {

        log.info("[ACCOUNT-TX] Debit card payment. txId={}, cardId={}, amount={}",
                tx.id(), tx.sourceProductId(), tx.amount());

        return cardEventPort.getById(tx.sourceProductId())
                .flatMapCompletable(card -> {

                    if (!card.isDebit()) {
                        return Completable.error(new AccountBusinessException("Card is not debit"));
                    }
                    if (!card.isActive()) {
                        return Completable.error(new AccountBusinessException("Debit card inactive"));
                    }

                    return repository.findById(card.accountId())
                            .switchIfEmpty(Single.error(new AccountNotFoundException("Linked account not found")))
                            .flatMap(account -> {
                                if (account.balance().compareTo(tx.amount()) < 0) {
                                    return Single.error(new InsufficientBalanceException("Insufficient balance"));
                                }
                                return repository.save(account.withBalance(account.balance().subtract(tx.amount())));
                            })
                            .flatMap(saved ->
                                    updateCache(saved)
                                            .andThen(Single.just(saved))
                            )
                            .flatMapCompletable(saved ->
                                    movementEventPort.registerMovement(tx, saved, "DEBIT_CARD_PAYMENT")
                                            .doOnComplete(() ->
                                                    log.info("[ACCOUNT-TX] Debit card payment movement registered. txId={}, accountId={}",
                                                            tx.id(), saved.id())
                                            )
                            );
                });
    }

    // ---------------------------------------------------------
    // CACHE UPDATE (Evict + Save)
    // ---------------------------------------------------------
    private Completable updateCache(Account account) {
        return cache.delete(account.id())
                .onErrorComplete(e -> {
                    log.warn("[ACCOUNT-TX] Cache eviction failed. accountId={}, reason={}",
                            account.id(), e.getMessage());
                    return true;
                })
                .andThen(cache.save(account)
                        .onErrorComplete(e -> {
                            log.warn("[ACCOUNT-TX] Cache save failed. accountId={}, reason={}",
                                    account.id(), e.getMessage());
                            return true;
                        })
                );
    }

    private record AccountTransferPair(Account source, Account target) {}
}
