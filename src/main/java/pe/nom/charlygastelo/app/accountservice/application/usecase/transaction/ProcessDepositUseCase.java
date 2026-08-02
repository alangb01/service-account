package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountDepositCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate.ValidateDepositUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountLedgerEventProducer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessDepositUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountLedgerEventProducer accountProducer;
    private final ValidateDepositUseCase validateDepositUseCase;
    private final TransactionEventProducer transactionProducer;

    public Completable execute(AccountDepositCommand cmd) {
        log.info("Processing deposit transaction: {}", cmd.transactionId());

        return validateDepositUseCase.validate(cmd)
                .andThen(Single.just(cmd))
                .flatMap(data ->
                        accountRepository.findById(data.targetAccountId())
                                .switchIfEmpty(Single.error(new RuntimeException("Target account not found")))
                                .map(account -> account.credit(data.amount()))
                                .flatMap(accountRepository::save)
                                .map(target -> new ProcessedTransaction(
                                        data.transactionId(),
                                        data.customerId(),
                                        data.amount(),
                                        null,
                                        target
                                ))
                ).flatMapCompletable(accountProducer::publishAccountDepositOccurred)
                .doOnComplete(() -> log.info("[ACCOUNT] transaction completed for txId={}", cmd.transactionId()))
                .onErrorResumeNext(err -> {
                    log.error("[ACCOUNT] transaction failed for txId={}, reason={}",
                            cmd.transactionId(), err.getMessage());


                    return transactionProducer.publishTransactionFailed(cmd.transactionId(), cmd.customerId(), err.getMessage())
                            .andThen(Completable.error(err));
                });
    }

}
