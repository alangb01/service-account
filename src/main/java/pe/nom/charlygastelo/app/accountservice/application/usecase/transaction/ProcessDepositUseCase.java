package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessDepositUseCase {
    private final AccountRepositoryPort accountRepository;
    private final TransactionEventProducer transactionEventProducer;

    public Completable execute(Transaction transaction) {
        log.info("Processing deposit transaction: {}", transaction.id());


        return validate(transaction)
                .andThen(accountRepository.findById(transaction.targetProductId())
                        .switchIfEmpty(Single.error(new RuntimeException("Account target not found")))
                )
                .map(account -> {
                    return account.credit(transaction.amount());
                })
                .flatMap(accountRepository::save)
                .flatMapCompletable(savedAccount ->
                        transactionEventProducer.publishTransactionCompleted(transaction)
                );

    }

    public Completable validate(Transaction transaction) {
        return Completable.complete();
    }
}
