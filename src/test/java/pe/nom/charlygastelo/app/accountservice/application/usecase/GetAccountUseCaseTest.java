package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class GetAccountUseCaseTest {

    private final AccountRepositoryPort repository = mock(AccountRepositoryPort.class);
    private final AccountCachePort cache = mock(AccountCachePort.class);

    private final GetAccountUseCase useCase =
            new GetAccountUseCase(repository, cache);

    @Test
    void shouldGetAccountFromCache() {
        Account account = account();

        when(cache.getById("acc-1"))
                .thenReturn(Maybe.just(account));

        useCase.byId("acc-1")
                .test()
                .assertComplete()
                .assertValue(account);

        verify(cache).getById("acc-1");
        verify(repository, never()).findById(anyString());
    }

    @Test
    void shouldGetAccountFromRepositoryWhenCacheEmpty() {
        Account account = account();

        when(cache.getById("acc-1"))
                .thenReturn(Maybe.empty());

        when(repository.findById("acc-1"))
                .thenReturn(Maybe.just(account));

        when(cache.save(account))
                .thenReturn(Completable.complete());

        useCase.byId("acc-1")
                .test()
                .assertComplete()
                .assertValue(account);

        verify(cache).getById("acc-1");
        verify(repository).findById("acc-1");
        verify(cache).save(account);
    }

    @Test
    void shouldFallbackToRepositoryWhenCacheFails() {
        Account account = account();

        when(cache.getById("acc-1"))
                .thenReturn(Maybe.error(new RuntimeException("Redis unavailable")));

        when(repository.findById("acc-1"))
                .thenReturn(Maybe.just(account));

        when(cache.save(account))
                .thenReturn(Completable.complete());

        useCase.byId("acc-1")
                .test()
                .assertComplete()
                .assertValue(account);

        verify(repository).findById("acc-1");
    }

    @Test
    void shouldFailWhenAccountNotFoundById() {
        when(cache.getById("acc-1"))
                .thenReturn(Maybe.empty());

        when(repository.findById("acc-1"))
                .thenReturn(Maybe.empty());

        useCase.byId("acc-1")
                .test()
                .assertError(AccountNotFoundException.class);
    }

    @Test
    void shouldGetAccountByNumberFromCache() {
        Account account = account();

        when(cache.getByNumber("001"))
                .thenReturn(Maybe.just(account));

        useCase.byNumber("number", "001")
                .test()
                .assertComplete()
                .assertValue(account);

        verify(cache).getByNumber("001");
        verify(repository, never()).findByNumber(anyString());
    }

    @Test
    void shouldGetAccountByNumberFromRepositoryWhenCacheEmpty() {
        Account account = account();

        when(cache.getByNumber("001"))
                .thenReturn(Maybe.empty());

        when(repository.findByNumber("001"))
                .thenReturn(Maybe.just(account));

        when(cache.save(account))
                .thenReturn(Completable.complete());

        useCase.byNumber("number", "001")
                .test()
                .assertComplete()
                .assertValue(account);

        verify(repository).findByNumber("001");
        verify(cache).save(account);
    }

    @Test
    void shouldFailWhenAccountNotFoundByNumber() {
        when(cache.getByNumber("001"))
                .thenReturn(Maybe.empty());

        when(repository.findByNumber("001"))
                .thenReturn(Maybe.empty());

        useCase.byNumber("number", "001")
                .test()
                .assertError(AccountNotFoundException.class);
    }

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
}