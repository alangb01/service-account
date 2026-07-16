package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate.ValidateTransferUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountLedgerEventProducer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessTransferUseCase {

    private final AccountRepositoryPort accountRepository;
    private final ValidateTransferUseCase validateTransferUseCase;

    private final AccountLedgerEventProducer accountProducer;
    private final TransactionEventProducer transactionProducer;

    public Completable execute(Transaction tx) {
        log.info("Processing transfer transaction: {}", tx.id());

        return validateTransferUseCase.validate(tx)
            .andThen(Single.just(tx))
            .flatMap(transaction ->
                accountRepository.findById(transaction.sourceProductId())
                    .switchIfEmpty(Single.error(
                        new RuntimeException("Source account not found")
                    ))
                    .map(source -> source.debit(transaction.amount()))
                    .flatMap(accountRepository::save)
                    .map(savedSource ->
                        new ProcessedTransaction(transaction, savedSource, null)
                    )
            )
            .flatMap(processed ->
                accountRepository.findById(processed.transaction().targetProductId())
                    .switchIfEmpty(Single.error(
                        new RuntimeException("Target account not found")
                    ))
                    .map(target -> target.credit(processed.transaction().amount()))
                    .flatMap(accountRepository::save)
                    .map(savedTarget ->
                        new ProcessedTransaction(
                            processed.transaction(),
                            processed.source(),
                            savedTarget
                        )
                    )
            )
            .flatMapCompletable(processed ->
                    accountProducer.publishAccountTransferOccurred(
                            processed.source(),
                            processed.target(),
                            processed.transaction()
                    )
            )
            .doOnComplete(() -> log.info("[ACCOUNT] transaction completed for txId={}", tx.id()))
            .onErrorResumeNext(err -> {
                log.error("[ACCOUNT] transaction failed for txId={}, reason={}",
                        tx.id(), err.getMessage());

                return transactionProducer.publishTransactionFailed(tx, err.getMessage())
                        .andThen(Completable.error(err));
            });
    }
}
