package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountLedgerEventProducer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessWithdrawUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountLedgerEventProducer accountProducer;
    private final TransactionEventProducer transactionProducer;

    public Completable execute(AccountWithdrawCommand cmd) {
        log.info("Processing withdraw tx: {}", cmd.transactionId());

        return  validateForWithdraw(cmd)
                .andThen(Single.just(cmd))
                .flatMap(data ->
                    accountRepository.findById(data.sourceAccountId())
                        .switchIfEmpty(Single.error(new BusinessException("Source account not found")))
                        .map(account -> account.debit(data.amount()))
                        .flatMap(accountRepository::save)
                        .map(source -> new ProcessedTransaction(
                                data.transactionId(),
                                data.customerId(),
                                data.amount(),
                                source,
                                null
                        ))

                )
                .flatMapCompletable(accountProducer::publishAccountWithdrawOccurred)
                .doOnComplete(() -> log.info("[ACCOUNT] transaction completed for txId={}", cmd.transactionId()))
                .onErrorResumeNext(err -> {
                    log.error("[ACCOUNT] transaction failed for txId={}, reason={}",
                            cmd.transactionId(), err.getMessage());

                    return transactionProducer.publishTransactionFailed(cmd.transactionId(), cmd.customerId(), err.getMessage())
                            .andThen(Completable.error(err));
                });
    }

    private Completable validateForWithdraw(AccountWithdrawCommand cmd) {
        return Completable.complete();
    }
}
