package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessTransferUseCase {
    private final AccountRepositoryPort accountRepository;
    private final TransactionEventProducer transactionEventProducer;

    public Completable execute(Transaction tx) {
        log.info("Processing deposit transaction: {}", tx.id());

        return accountRepository.findById(tx.sourceProductId())
                .switchIfEmpty(Single.error(new RuntimeException("Source account not found")))
                .flatMap(sourceAccount -> {
                    if(!sourceAccount.customerId().equals(tx.customerId())) {
                        return Single.error(new RuntimeException("Account must belong to this customer "+sourceAccount.customerId()));
                    }

                    if (!sourceAccount.hasEnoughBalance(tx.amount())) {
                        return Single.error(new RuntimeException("Insufficient balance"));
                    }

                    sourceAccount.debit(tx.amount());
                    return accountRepository.save(sourceAccount);
                })
                .flatMap(savedSource ->
                        accountRepository.findById(tx.targetProductId())
                                .switchIfEmpty(Single.error(new RuntimeException("Target account not found")))
                )
                .flatMap(targetAccount -> {
                    if(!targetAccount.customerId().equals(tx.customerId())) {
                        return Single.error(new RuntimeException("Account must belong to this customer "+targetAccount.customerId()));
                    }

                    targetAccount.credit(tx.amount());
                    return accountRepository.save(targetAccount);
                })
                .flatMapCompletable(savedTarget ->
                        transactionEventProducer.publishTransactionCompleted(tx)
                );

    }

    public Completable validate(Transaction transaction) {
        return Completable.complete();
    }
}
