package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;



@Component
@RequiredArgsConstructor
@Slf4j
public class CreatedTransactionUseCase {
    private final ProcessCreditPaymentUseCase creditPaymentUseCase;
    private final ProcessDebitCardPaymentUseCase debitCardPaymentUseCase;
    private final ProcessDebitCardPurchaseUseCase debitCardPurchaseUseCase;
    private final ProcessYankiPaymentUseCase yankiPaymentUseCase;
    private final ProcessTransferUseCase transferUseCase;
    private final ProcessDepositUseCase depositUseCase;
    private final ProcessWithdrawUseCase withdrawUseCase;

    public boolean isAccountServiceResponsible(TransactionType type) {
        return switch (type) {
            case
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
    public Completable execute(Transaction tx) {

        log.info("[ACCOUNT] Routing txId={} type={} to correct use case", tx.id(), tx.type());

        return switch (tx.type()) {
//            case DEBIT_CARD_PURCHASE -> debitCardPurchaseUseCase.execute(tx);

//            case DEBIT_CARD_PAYMENT -> debitCardPaymentUseCase.execute(tx);

//            case YANKI_PAYMENT -> yankiPaymentUseCase.execute(tx);

            case TRANSFER, TRANSFER_TO_THIRD -> transferUseCase.execute(tx);

            case DEPOSIT -> depositUseCase.execute(tx);

            case WITHDRAW -> withdrawUseCase.execute(tx);

            default -> {
                log.warn("[ACCOUNT] txId={} type={} ignored (not account responsibility)", tx.id(), tx.type());
                yield Completable.complete();
            }
        };
    }
}
