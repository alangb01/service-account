package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class ListAccountsUseCaseTest {

    private AccountRepositoryPort repository;
    private AccountCachePort cache;
    private ListAccountsUseCase useCase;

    @BeforeEach
    void setup() {
        repository = mock(AccountRepositoryPort.class);
        cache = mock(AccountCachePort.class);
        useCase = new ListAccountsUseCase(repository, cache);
    }

    // ---------------------------------------------------------
    // TEST: LIST ALL ACCOUNTS
    // ---------------------------------------------------------
    @Test
    void shouldListAllAccounts() {

        when(repository.findAll())
                .thenReturn(Flowable.just(account()));

        when(cache.save(any(Account.class)))
                .thenReturn(Completable.complete());

        useCase.all()
                .test()
                .assertComplete()
                .assertValueCount(1)
                .assertValue(a -> a.id().equals("acc-1"));

        verify(repository).findAll();
        verify(cache).save(any(Account.class));
    }

    // ---------------------------------------------------------
    // TEST: LIST ACCOUNTS BY CUSTOMER
    // ---------------------------------------------------------
    @Test
    void shouldListAccountsByCustomer() {

        when(repository.findByCustomerId("cus-1"))
                .thenReturn(Flowable.just(account()));

        when(cache.save(any(Account.class)))
                .thenReturn(Completable.complete());

        useCase.byCustomer("cus-1")
                .test()
                .assertComplete()
                .assertValueCount(1)
                .assertValue(a -> a.customerId().equals("cus-1"));

        verify(repository).findByCustomerId("cus-1");
        verify(cache).save(any(Account.class));
    }

    // ---------------------------------------------------------
    // TEST: REDIS FAILURE SHOULD NOT BREAK FLOW
    // ---------------------------------------------------------
    @Test
    void shouldContinueWhenRedisFails() {

        when(repository.findAll())
                .thenReturn(Flowable.just(account()));

        when(cache.save(any(Account.class)))
                .thenReturn(Completable.error(new RuntimeException("Redis down")));

        useCase.all()
                .test()
                .assertComplete()
                .assertValueCount(1);

        verify(repository).findAll();
        verify(cache).save(any(Account.class));
    }

    // ---------------------------------------------------------
    // TEST: MULTIPLE ACCOUNTS
    // ---------------------------------------------------------
    @Test
    void shouldListMultipleAccounts() {

        when(repository.findAll())
                .thenReturn(Flowable.just(account(), account2()));

        when(cache.save(any(Account.class)))
                .thenReturn(Completable.complete());

        useCase.all()
                .test()
                .assertComplete()
                .assertValueCount(2);

        verify(repository).findAll();
        verify(cache, times(2)).save(any(Account.class));
    }

    // ---------------------------------------------------------
    // FACTORY METHODS
    // ---------------------------------------------------------
    private Account account() {
        return new Account(
                "acc-1",
                "cus-1",
                "PERSONAL",
                "001",
                AccountType.SAVINGS,
                BigDecimal.TEN,
                "PEN",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                true,
                AccountStatus.ACTIVE
        );
    }

    private Account account2() {
        return new Account(
                "acc-2",
                "cus-1",
                "PERSONAL",
                "002",
                AccountType.CHECKING,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                true,
                AccountStatus.ACTIVE
        );
    }
}
