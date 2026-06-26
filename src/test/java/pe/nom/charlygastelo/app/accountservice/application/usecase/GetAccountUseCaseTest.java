package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAccountUseCaseTest {
    private final AccountRepositoryPort accountRepository = mock(AccountRepositoryPort.class);
    private final AccountCachePort cache = mock(AccountCachePort.class);
    private final GetAccountUseCase getAccountUseCase = new GetAccountUseCase(accountRepository, cache);

    @Test
    void byIdShouldReturnAccountWhenExists() {
        String accountId = "account-001";

        Account account = new Account(
                accountId,
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(cache.getById(accountId)).thenReturn(Maybe.empty());
        when(accountRepository.findById(accountId)).thenReturn(Maybe.just(account));
        when(cache.save(account)).thenReturn(Completable.complete());

        TestObserver<Account> observer = getAccountUseCase.byId(accountId).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(account);

        verify(cache).getById(accountId);
        verify(accountRepository).findById(accountId);
        verify(cache).save(account);
    }

    @Test
    void byIdShouldReturnFromCacheWhenExists() {
        String accountId = "account-001";

        Account account = new Account(
                accountId,
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(cache.getById(accountId)).thenReturn(Maybe.just(account));
        when(accountRepository.findById(accountId)).thenReturn(Maybe.empty());

        TestObserver<Account> observer = getAccountUseCase.byId(accountId).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(account);

        verify(cache).getById(accountId);
    }

    @Test
    void byIdShouldReturnEmptyWhenAccountDoesNotExist() {
        String accountId = "account-999";

        when(cache.getById(accountId)).thenReturn(Maybe.empty());
        when(accountRepository.findById(accountId)).thenReturn(Maybe.empty());

        TestObserver<Account> observer = getAccountUseCase.byId(accountId).test();

        observer.assertError(throwable -> throwable.getMessage().contains("not found"));
        verify(cache).getById(accountId);
        verify(accountRepository).findById(accountId);
    }

    @Test
    void byNumberShouldReturnAccountWhenExists() {
        String accountNumber = "ACC-001";

        Account account = new Account(
                "account-001",
                "customer-001",
                accountNumber,
                AccountType.SAVINGS,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(cache.getByNumber(accountNumber)).thenReturn(Maybe.empty());
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Maybe.just(account));
        when(cache.save(account)).thenReturn(Completable.complete());

        TestObserver<Account> observer = getAccountUseCase.byNumber("DOC_TYPE", accountNumber).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(account);

        verify(cache).getByNumber(accountNumber);
        verify(accountRepository).findByNumber(accountNumber);
        verify(cache).save(account);
    }

    @Test
    void byNumberShouldReturnFromCacheWhenExists() {
        String accountNumber = "ACC-001";

        Account account = new Account(
                "account-001",
                "customer-001",
                accountNumber,
                AccountType.SAVINGS,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(cache.getByNumber(accountNumber)).thenReturn(Maybe.just(account));
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Maybe.empty());

        TestObserver<Account> observer = getAccountUseCase.byNumber("DOC_TYPE", accountNumber).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(account);

        verify(cache).getByNumber(accountNumber);
    }

    @Test
    void byNumberShouldReturnEmptyWhenAccountDoesNotExist() {
        String accountNumber = "ACC-999";

        when(cache.getByNumber(accountNumber)).thenReturn(Maybe.empty());
        when(accountRepository.findByNumber(accountNumber)).thenReturn(Maybe.empty());

        TestObserver<Account> observer = getAccountUseCase.byNumber("DOC_TYPE", accountNumber).test();

        observer.assertError(throwable -> throwable.getMessage().contains("not found"));
        verify(cache).getByNumber(accountNumber);
        verify(accountRepository).findByNumber(accountNumber);
    }
}
