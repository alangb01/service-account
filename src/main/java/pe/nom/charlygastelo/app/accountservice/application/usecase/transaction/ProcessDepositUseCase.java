package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountLedgerEventProducer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessDepositUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountLedgerEventProducer accountProducer;
    private final TransactionEventProducer transactionProducer;

    public Completable execute(Transaction transaction) {
        log.info("Processing deposit transaction: {}", transaction.id());

        return Single.fromCallable(() -> {
                    transaction.validateForDeposit();
                    return transaction;
                })
                .flatMap(tx ->
                        accountRepository.findById(tx.targetProductId())
                                .switchIfEmpty(Single.error(new RuntimeException("Target account not found")))
                                .map(account -> account.credit(tx.amount()))
                                .flatMap(accountRepository::save)
                                .map(target -> new ProcessedTransaction(tx, null,target))
                ).flatMapCompletable(processedTransaction ->
                    accountProducer.publishAccountDepositOccurred(
                            processedTransaction.target(),
                            processedTransaction.transaction()
                        )
                )
                .doOnComplete(() -> log.info("[ACCOUNT] transaction completed for txId={}", transaction.id()))
                .onErrorResumeNext(err -> {
                    log.error("[ACCOUNT] transaction failed for txId={}, reason={}",
                            transaction.id(), err.getMessage());

                    return transactionProducer.publishTransactionFailed(transaction, err.getMessage())
                            .andThen(Completable.error(err));
                });
    }
}
