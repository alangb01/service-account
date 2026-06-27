package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountBusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.MovementEventPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.TransactionEventPort;

@RequiredArgsConstructor
@Slf4j
public class ProcessTransactionUseCase {

    private final AccountRepositoryPort repository;
    private final MovementEventPort movementEventPort;
    private final TransactionEventPort transactionEventPort;

    public Completable execute(Transaction transaction) {
        log.info("Processing transaction. id={}, type={}",
                transaction.id(), transaction.type());

        return process(transaction)
                .andThen(transactionEventPort.publishTransactionCompleted(transaction))
                .doOnComplete(() ->
                        log.info("Transaction processed successfully. id={}", transaction.id()))
                .onErrorResumeNext(error -> {
                    log.error("Transaction processing failed. id={}, reason={}",
                            transaction.id(), error.getMessage(), error);

                    return transactionEventPort
                            .publishTransactionFailed(transaction, error.getMessage())
                            .andThen(Completable.error(error));
                });
    }

    private Completable process(Transaction transaction) {
        return switch (transaction.type()) {
            case DEPOSIT -> deposit(transaction);
            case WITHDRAWAL -> withdraw(transaction);
            case TRANSFER -> transfer(transaction);
            default -> Completable.complete();
        };
    }

    private Completable deposit(Transaction tx) {
        return repository.findById(tx.sourceProductId())
                .switchIfEmpty(Single.error(new AccountNotFoundException("Account not found")))
                .flatMap(account ->
                        repository.save(account.withBalance(account.balance().add(tx.amount())))
                )
                .flatMapCompletable(account ->
                        movementEventPort.registerMovement(tx, account, "DEPOSIT")
                );
    }

    private Completable withdraw(Transaction tx) {
        return repository.findById(tx.sourceProductId())
                .switchIfEmpty(Single.error(new AccountNotFoundException("Account not found")))
                .flatMap(account -> {
                    if (account.balance().compareTo(tx.amount()) < 0) {
                        return Single.error(new AccountBusinessException("Insufficient balance"));
                    }

                    return repository.save(
                            account.withBalance(account.balance().subtract(tx.amount()))
                    );
                })
                .flatMapCompletable(account ->
                        movementEventPort.registerMovement(tx, account, "WITHDRAWAL")
                );
    }

    private Completable transfer(Transaction tx) {
        return withdraw(tx)
                .andThen(repository.findById(tx.targetProductId())
                        .switchIfEmpty(Single.error(new AccountNotFoundException("Target account not found")))
                        .flatMap(account ->
                                repository.save(account.withBalance(account.balance().add(tx.amount())))
                        )
                        .flatMapCompletable(account ->
                                movementEventPort.registerMovement(tx, account, "TRANSFER_IN")
                        ));
    }
}