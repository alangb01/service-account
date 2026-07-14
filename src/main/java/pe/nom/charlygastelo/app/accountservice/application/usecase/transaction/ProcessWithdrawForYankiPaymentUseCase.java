package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessWithdrawForYankiPaymentUseCase {

    private final AccountRepositoryPort accountRepository;

    public Single<ProcessedTransaction> execute(Transaction tx) {

        log.info("[ACCOUNT] Processing withdraw for Yanki payment. txId={}, accountId={}, amount={}",
                tx.id(), tx.sourceProductId(), tx.amount());

        return Single.fromCallable(() -> {
                    tx.validateForYankiPayment();   // Validación en dominio
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
                                .map(updatedSource ->
                                        new ProcessedTransaction(transaction, updatedSource, null)
                                )
                )
                .doOnSuccess(result ->
                        log.info("[ACCOUNT] Yanki withdraw completed. txId={}, accountId={}",
                                result.transaction().id(), result.source().id())
                )
                .doOnError(error ->
                        log.error("[ACCOUNT] Yanki withdraw failed. txId={}, reason={}",
                                tx.id(), error.getMessage())
                );
    }
}
