package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountLedgerEventProducer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessDebitCardPaymentUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountLedgerEventProducer accountProducer;
    private final TransactionEventProducer transactionProducer;

    public Completable execute(Transaction tx) {

        log.info("[ACCOUNT] Processing withdraw for debit card payment. txId={}, accountId={}, amount={}",
                tx.id(), tx.sourceProductId(), tx.amount());

        return Single.fromCallable(() -> {
                    tx.validateForDebitCardPayment();   // Validación en dominio
                    return tx;
                })
                .flatMap(transaction ->
                    accountRepository.findById(transaction.sourceProductId())
                        .switchIfEmpty(Single.error(new RuntimeException("Source account not found")))
                        .map(account -> {
                            if (!account.customerId().equals(transaction.customerId())) {
                                throw new RuntimeException("Account does not belong to this customer");
                            }
                            return account.debit(transaction.amount()); // Invariante de dominio
                        })
                        .flatMap(accountRepository::save)
                        .map(updatedAccount ->
                                new ProcessedTransaction(transaction, updatedAccount, null)
                        )
                )
                .flatMapCompletable(processedTransaction ->
                    accountProducer.publishDebitCardPaymentCompleted(
                        processedTransaction.target(),
                        processedTransaction.transaction()
                    )
                )
                .doOnComplete(() -> log.info("[ACCOUNT] transaction completed for txId={}", tx.id()))
                .onErrorResumeNext(err -> {
                    log.error("[ACCOUNT] transaction failed for txId={}, reason={}",
                            tx.id(), err.getMessage());

                    return transactionProducer.publishTransactionFailed(tx, err.getMessage())
                            .andThen(Completable.error(err));
                });
    }
}
