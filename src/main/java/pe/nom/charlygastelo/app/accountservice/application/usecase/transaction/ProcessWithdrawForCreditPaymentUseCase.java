package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessWithdrawForCreditPaymentUseCase {

    private final AccountRepositoryPort accountRepository;

    public Single<ProcessedTransaction> execute(Transaction tx) {

        log.info("[ACCOUNT] Processing withdraw for credit payment. txId={}, accountId={}, amount={}",
                tx.id(), tx.sourceProductId(), tx.amount());

        return Single.fromCallable(() -> {
                    tx.validateForCreditPayment();   // Validación en dominio
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
                .doOnSuccess(result ->
                        log.info("[ACCOUNT] Withdraw completed. txId={}, accountId={}",
                                result.transaction().id(), result.source().id())
                )
                .doOnError(error ->
                        log.error("[ACCOUNT] Withdraw failed. txId={}, reason={}",
                                tx.id(), error.getMessage())
                );
    }
}
