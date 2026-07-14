package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessWithdrawUseCase {

    private final AccountRepositoryPort accountRepository;

    public Single<ProcessedTransaction> execute(Transaction transaction) {
        log.info("Processing withdraw transaction: {}", transaction.id());

        return Single.fromCallable(() -> {
                    transaction.validateForWithdraw();
                    return transaction;
                })
                .flatMap(tx ->
                        accountRepository.findById(tx.sourceProductId())
                                .switchIfEmpty(Single.error(new RuntimeException("Source account not found")))
                                .map(account -> account.debit(tx.amount()))
                                .flatMap(accountRepository::save)
                                .map(source -> new ProcessedTransaction(tx, source, null))
                );
    }
}
