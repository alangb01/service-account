package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class DeleteAccountUseCaseTest {

    private final AccountRepositoryPort repository = mock(AccountRepositoryPort.class);
    private final AccountEventProducerPort producer = mock(AccountEventProducerPort.class);
    private final AccountCachePort cache = mock(AccountCachePort.class);

    private final DeleteAccountUseCase useCase =
            new DeleteAccountUseCase(repository, producer, cache);

    @Test
    void shouldDeleteAccountSuccessfully() {
        Account account = account();

        when(repository.findById("acc-1")).thenReturn(Maybe.just(account));
        when(repository.deleteById("acc-1")).thenReturn(Completable.complete());
        when(cache.delete("acc-1")).thenReturn(Completable.complete());
        when(producer.publishAccountDeleted(account.id())).thenReturn(Completable.complete());

        useCase.execute("acc-1")
                .test()
                .assertComplete();

        verify(repository).deleteById("acc-1");
        verify(producer).publishAccountDeleted(account.id());
    }

    @Test
    void shouldFailWhenAccountNotFound() {
        when(repository.findById("acc-1")).thenReturn(Maybe.empty());

        useCase.execute("acc-1")
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
                BigDecimal.ZERO,
                "PEN",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                true,
                AccountStatus.ACTIVE
        );
    }
}