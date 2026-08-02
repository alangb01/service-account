package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountTransferCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter.AccountRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateTransferUseCase {

    private final AccountRepository accountRepository;

    public Completable validate(AccountTransferCommand cmd) {

        log.info("Validating transfer txId={}, customerId={}",
                cmd.transactionId(), cmd.customerId());

        return Completable.mergeArray(
                validateAmount(cmd),
                validateSourceProduct(cmd),
                validateTargetProduct(cmd),
                validateTransferCompatibility(cmd)
        )
        .doOnComplete(() ->
                log.info("Validation OK txId={}", cmd.transactionId())
        )
        .doOnError(err ->
                log.error("Validation FAILED txId={}, reason={}",
                        cmd.transactionId(), err.getMessage())
        );
    }

    public Completable validateToThird(AccountTransferCommand cmd) {

        log.info("[VALIDATE-TRANSFER] Validating transfer txId={}, customerId={}",
                cmd.transactionId(), cmd.customerId());

        return Completable.mergeArray(
                        validateAmount(cmd),
                        validateSourceProduct(cmd),
                        validateTargetProduct(cmd),
                        validateTransferCompatibility(cmd)
                )
                .doOnComplete(() ->
                        log.info("[VALIDATE-TRANSFER] Validation OK txId={}", cmd.transactionId())
                )
                .doOnError(err ->
                        log.error("[VALIDATE-TRANSFER] Validation FAILED txId={}, reason={}",
                                cmd.transactionId(), err.getMessage())
                );
    }


    private Completable validateAmount(AccountTransferCommand cmd) {
        return Completable.fromAction(() -> {
            if (cmd.amount() == null || cmd.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
        });
    }

    private Completable validateSourceProduct(AccountTransferCommand cmd) {
        return accountRepository.findById(cmd.sourceAccountId())
                .switchIfEmpty(Single.error(new RuntimeException("Source product not found")))
                .flatMapCompletable(account -> {
                    if(!account.customerId().equals(cmd.customerId())){
                        return Completable.error(new RuntimeException("Source product is not own account"));
                    }

                    if (!account.isActive()) {
                        return Completable.error(new RuntimeException("Source product inactive"));
                    }
                    return Completable.complete();
                });
    }

    private Completable validateTargetProduct(AccountTransferCommand cmd) {
        return accountRepository.findById(cmd.targetAccountId())
                .switchIfEmpty(Single.error(new RuntimeException("Target product not found")))
                .flatMapCompletable(account -> {

                    if (!account.isActive()) {
                        return Completable.error(new RuntimeException("Target product inactive"));
                    }
                    return Completable.complete();
                });
    }


    private Completable validateTransferCompatibility(AccountTransferCommand cmd) {
        return Completable.fromAction(() -> {
            String source = cmd.sourceAccountId();
            String target = cmd.targetAccountId();

            if (source.equals("ACCOUNT") && target.equals("ACCOUNT")) return;
            if (source.equals("CARD") && target.equals("ACCOUNT")) return;
            if (source.equals("CREDIT") && target.equals("ACCOUNT")) return;

            throw new IllegalArgumentException("Transfer type not allowed: " + source + " -> " + target);
        });
    }

}
