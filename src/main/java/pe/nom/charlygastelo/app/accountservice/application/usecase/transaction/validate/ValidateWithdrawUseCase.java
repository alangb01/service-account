package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountDepositCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter.AccountRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateWithdrawUseCase {

    private final AccountRepository accountRepository;

    public Completable validate(AccountWithdrawCommand cmd) {

        log.info("validating withdraw txId={}, customerId={}",
                cmd.transactionId(), cmd.customerId());

        return Completable.mergeArray(
                validateAmount(cmd),
                validateCustomerOwner(cmd),
                validateTargetAccount(cmd)
        )
        .doOnComplete(() ->
                log.info("Validation OK txId={}", cmd.transactionId())
        )
        .doOnError(err ->
                log.error("Validation FAILED txId={}, reason={}",
                        cmd.transactionId(), err.getMessage())
        );
    }


    private Completable validateAmount(AccountWithdrawCommand cmd) {
        return Completable.fromAction(() -> {
            if (cmd.amount() == null || cmd.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
        });
    }

    private Completable validateTargetAccount(AccountWithdrawCommand cmd) {
        return accountRepository.findById(cmd.sourceAccountId())
                .switchIfEmpty(Single.error(new RuntimeException("Target product not found")))
                .flatMapCompletable(account -> {
                    if (!account.isActive()) {
                        return Completable.error(new RuntimeException("Target product inactive"));
                    }
                    return Completable.complete();
                });
    }

    private Completable validateCustomerOwner(AccountWithdrawCommand cmd) {
        return Completable.fromAction(()->{
            log.info("validando customer owner");
        });
    }
}
