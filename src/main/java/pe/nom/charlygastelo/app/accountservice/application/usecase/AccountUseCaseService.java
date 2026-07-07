package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CustomerClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.CreateAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.DeleteAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.UpdateAccountUseCasePort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountUseCaseService implements FindAccountUseCasePort, UpdateAccountUseCasePort, DeleteAccountUseCasePort, CreateAccountUseCasePort {

    private final AccountRepositoryPort accountRepository;
    private final CustomerClientPort customerClient;

    @Override
    public Maybe<Account> findById(String id) {
        log.debug("Finding account by id. id={}", id);

        return accountRepository.findById(id)
                .doOnSuccess(account -> log.info("Account found. id={}", account.id()))
                .doOnError(error -> log.error("Error occurred while fetching account. id={}", id, error));
    }

    @Override
    public Single<List<Account>> findAll() {

        log.debug("Finding all accounts.");
        return accountRepository.findAll().toList();
    }

    @Override
    public Single<List<Account>> findByCustomerId(String customerId) {
        log.debug("Finding accounts by customer id. customerId={}", customerId);
        return accountRepository.findByCustomerId(customerId).toList();
    }


    @Override
    public Single<Account> create(Account account, String token ) {
        log.info("[AccountUseCaseService] Creating account. customerId={}", account.customerId());

        return customerClient.getById(account.customerId(), token)
                .flatMap(customer ->{
                    log.info("[AccountUseCaseService] creating account: customerId={}", account.customerId());
                    Account newAccount= account.createWith(account);
                    return accountRepository.save(newAccount);
                });
    }

    @Override
    public Completable delete(String id) {
        log.info("Deleting account. accountId={}", id);
        return accountRepository.findById(id)
                .switchIfEmpty(Single.error(new BusinessException("Account not found.")))
                .flatMapCompletable(account -> accountRepository.deleteById(id))
                .doOnComplete(() -> log.info("Account deleted successfully. accountId={}", id));
    }

    @Override
    public Single<Account> update(String id, Account request) {
        return accountRepository.findById(id)
                .switchIfEmpty(Single.error(new BusinessException("Account not found.")))
                .flatMap(existing -> {
                    Account updated = existing.updateWith(request);
                    return accountRepository.save(updated);
                })
                .doOnSuccess(updated -> log.info("Account updated successfully. accountId={}", updated.id()))
                .doOnError(error -> log.error("Error occurred while updating account. accountId={}", id, error));
    }
}
