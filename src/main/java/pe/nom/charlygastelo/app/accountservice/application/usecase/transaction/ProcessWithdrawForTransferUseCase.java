package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;
@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessWithdrawForTransferUseCase {

    private final AccountRepositoryPort accountRepository;

    public Single<ProcessedTransaction> execute(Transaction tx) {
        log.info("Processing transfer transaction: {}", tx.id());

        return Single.fromCallable(() -> {
                    tx.validateForTransfer();
                    return tx;
                })
                .flatMap(transaction ->
                        accountRepository.findById(transaction.sourceProductId())
                                .switchIfEmpty(Single.error(new RuntimeException("Source account not found")))
                                .map(source -> {
                                    if (!source.customerId().equals(transaction.customerId())) {
                                        throw new RuntimeException("Source account must belong to this customer");
                                    }
                                    return source.debit(transaction.amount());
                                })
                                .flatMap(accountRepository::save)
                                .flatMap(savedSource ->
                                        accountRepository.findById(transaction.targetProductId())
                                                .switchIfEmpty(Single.error(new RuntimeException("Target account not found")))
                                                .map(target -> {
                                                    if (!target.customerId().equals(transaction.customerId())) {
                                                        throw new RuntimeException("Target account must belong to this customer");
                                                    }
                                                    return target.credit(transaction.amount());
                                                })
                                                .flatMap(accountRepository::save)
                                                .map(savedTarget ->
                                                        new ProcessedTransaction(transaction, savedSource, savedTarget)
                                                )
                                )
                );
    }
}
