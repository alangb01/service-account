package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter.AccountRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateTransferUseCase {

    private final AccountRepository accountRepository;

    public Completable validate(Transaction tx) {

        log.info("[VALIDATE-TRANSFER] Validating transfer txId={}, customerId={}",
                tx.id(), tx.customerId());

        return Completable.mergeArray(
                validateAmount(tx),
                validateSourceProduct(tx),
                validateTargetProduct(tx),
//                validateCustomerDebt(tx),
//                validateProfileRules(tx),
                validateTransferCompatibility(tx)
//                validateIdempotency(tx),
//                validateCorrelation(tx),
//                validateLimits(tx)
        )
        .doOnComplete(() ->
                log.info("[VALIDATE-TRANSFER] Validation OK txId={}", tx.id())
        )
        .doOnError(err ->
                log.error("[VALIDATE-TRANSFER] Validation FAILED txId={}, reason={}",
                        tx.id(), err.getMessage())
        );
    }

    public Completable validateTranferToThird(Transaction tx) {

        log.info("[VALIDATE-TRANSFER] Validating transfer to third txId={}, customerId={}",
                tx.id(), tx.customerId());

        return Completable.mergeArray(
                        validateAmount(tx),
                        validateSourceProduct(tx),
                        validateTargetProduct(tx),
//                validateCustomerDebt(tx),
//                validateProfileRules(tx),
                        validateTransferCompatibility(tx)
//                validateIdempotency(tx),
//                validateCorrelation(tx),
//                validateLimits(tx)
                )
                .doOnComplete(() ->
                        log.info("[VALIDATE-TRANSFER-TO-THIRD] Validation OK txId={}", tx.id())
                )
                .doOnError(err ->
                        log.error("[VALIDATE-TRANSFER-TO-THIRD] Validation FAILED txId={}, reason={}",
                                tx.id(), err.getMessage())
                );
    }

    private Completable validateAmount(Transaction tx) {
        return Completable.fromAction(() -> {
            if (tx.amount() == null || tx.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
        });
    }

    private Completable validateSourceProduct(Transaction tx) {
        return accountRepository.findById(tx.sourceProductId())
                .switchIfEmpty(Single.error(new RuntimeException("Source product not found")))
                .flatMapCompletable(account -> {
                    if(!account.customerId().equals(tx.customerId())){
                        return Completable.error(new RuntimeException("Source product is not own account"));
                    }

                    if (!account.isActive()) {
                        return Completable.error(new RuntimeException("Source product inactive"));
                    }
                    return Completable.complete();
                });
    }

    private Completable validateTargetProduct(Transaction tx) {
        return accountRepository.findById(tx.targetProductId())
                .switchIfEmpty(Single.error(new RuntimeException("Target product not found")))
                .flatMapCompletable(account -> {
                    if(tx.type().equals(TransactionType.TRANSFER) && !account.customerId().equals(tx.customerId())) {
                        return Completable.error(new RuntimeException("Target product is not own account"));
                    }

                    if (!account.isActive()) {
                        return Completable.error(new RuntimeException("Target product inactive"));
                    }
                    return Completable.complete();
                });
    }

//    private Completable validateCustomerDebt(Transaction tx) {
//        return customerValidationPort.hasDebt(tx.customerId())
//                .flatMapCompletable(hasDebt -> {
//                    if (hasDebt) {
//                        return Completable.error(new RuntimeException("Customer has overdue debt"));
//                    }
//                    return Completable.complete();
//                });
//    }

//    private Completable validateProfileRules(Transaction tx) {
//        return profileValidationPort.validateProfile(tx.customerId());
//    }

    private Completable validateTransferCompatibility(Transaction tx) {
        return Completable.fromAction(() -> {
            String source = tx.sourceProductType();
            String target = tx.targetProductType();

            if (source.equals("ACCOUNT") && target.equals("ACCOUNT")) return;
            if (source.equals("CARD") && target.equals("ACCOUNT")) return;
            if (source.equals("CREDIT") && target.equals("ACCOUNT")) return;

            throw new IllegalArgumentException("Transfer type not allowed: " + source + " -> " + target);
        });
    }

//    private Completable validateIdempotency(Transaction tx) {
//        return transactionRepository.findById(tx.id())
//                .flatMapCompletable(existing ->
//                        Completable.error(new RuntimeException("Duplicate transaction"))
//                )
//                .onErrorComplete(); // si no existe, OK
//    }
//
//    private Completable validateCorrelation(Transaction tx) {
//        return Completable.fromAction(() -> {
//            if (tx.correlationId() == null || tx.correlationId().isBlank()) {
//                throw new IllegalArgumentException("CorrelationId is required");
//            }
//        });
//    }

//    private Completable validateLimits(Transaction tx) {
//        return limitValidationPort.validateTransferLimits(tx.customerId(), tx.amount());
//    }
}
