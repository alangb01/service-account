package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;



@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessCreatedTransactionUseCase {
    private final ProcessWithdrawForCreditPaymentUseCase withdrawCreditPaymentUseCase;
    private final ProcessWithdrawForDebitCardPaymentUseCase withdrawDebitCardUseCase;
    private final ProcessWithdrawForYankiPaymentUseCase withdrawYankiUseCase;
    private final ProcessWithdrawForTransferUseCase withdrawTransferUseCase;
    private final ProcessDepositUseCase depositUseCase;
    private final ProcessWithdrawUseCase withdrawUseCase;

    public boolean isAccountServiceResponsible(TransactionType type) {
        return switch (type) {
            case CREDIT_PAYMENT,
                 DEBIT_CARD_PAYMENT,
                 YANKI_PAYMENT,
                 TRANSFER,
                 TRANSFER_TO_THIRD,
                 DEPOSIT,
                 WITHDRAW -> true;

            default -> false; // CREDIT_WITHDRAW, CREDIT_CARD_CHARGE
        };
    }

    /**
     * Ejecuta el caso de uso correcto según el tipo de transacción.
     */
    public Single<ProcessedTransaction> execute(Transaction tx) {

        log.info("[ACCOUNT] Routing txId={} type={} to correct use case", tx.id(), tx.type());

        return switch (tx.type()) {
            case CREDIT_PAYMENT -> withdrawCreditPaymentUseCase.execute(tx);

            case DEBIT_CARD_PAYMENT -> withdrawDebitCardUseCase.execute(tx);

            case YANKI_PAYMENT -> withdrawYankiUseCase.execute(tx);

            case TRANSFER, TRANSFER_TO_THIRD -> withdrawTransferUseCase.execute(tx);

            case DEPOSIT -> depositUseCase.execute(tx);

            case WITHDRAW -> withdrawUseCase.execute(tx);

            default -> {
                log.warn("[ACCOUNT] txId={} type={} ignored (not account responsibility)", tx.id(), tx.type());
                // Devuelve un ProcessedTransaction vacío pero válido
                yield Single.just(
                        new ProcessedTransaction(tx, null, null)
                );
            }
        };
    }
}
