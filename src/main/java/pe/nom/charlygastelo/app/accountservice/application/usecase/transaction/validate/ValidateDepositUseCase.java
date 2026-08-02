package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountDepositCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter.AccountRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateDepositUseCase {

    private final AccountRepository accountRepository;

    public Completable validate(AccountDepositCommand cmd) {

        log.info("Validating AccountDepositCommand cmdId={}, customerId={}",
                cmd.transactionId(), cmd.customerId());

        return Completable.mergeArray(
                validateAmount(cmd),
                validateTargetAccount(cmd),
                validateCustomerOwner(cmd)
        )
        .doOnComplete(() ->
                log.info("Validation OK txId={}", cmd.transactionId())
        )
        .doOnError(err ->
                log.error("Validation FAILED txId={}, reason={}",
                        cmd.transactionId(), err.getMessage())
        );
    }

    private Completable validateCustomerOwner(AccountDepositCommand cmd) {
        return Completable.fromAction(()->{
                log.info("validando customer owner");
            });
    }

    private Completable validateAmount(AccountDepositCommand cmd) {
        return Completable.fromAction(() -> {
            if (cmd.amount() == null || cmd.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
        });
    }

    private Completable validateTargetAccount(AccountDepositCommand cmd) {
        return accountRepository.findById(cmd.targetAccountId())
                .switchIfEmpty(Single.error(new RuntimeException("Target product not found")))
                .flatMapCompletable(account -> {
                    if (!account.isActive()) {
                        return Completable.error(new RuntimeException("Target product inactive"));
                    }
                    return Completable.complete();
                });
    }

}
