package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper.AccountPersistentMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class AccountRepositoryAdapterTest {

    private final ReactiveAccountRepository repository = mock(ReactiveAccountRepository.class);
    private final AccountPersistentMapper mapper = new AccountPersistentMapper();
    private final AccountRepositoryAdapter adapter =
            new AccountRepositoryAdapter(repository, mapper);

    @Test
    void shouldSaveAccount() {
        Account account = account();
        AccountDocument document = mapper.toDocument(account);

        when(repository.save(any(AccountDocument.class)))
                .thenReturn(Mono.just(document));

        adapter.save(account)
                .test()
                .assertComplete()
                .assertValue(saved -> saved.id().equals("acc-1"));

        verify(repository).save(any(AccountDocument.class));
    }

    @Test
    void shouldFindAccountById() {
        Account account = account();
        AccountDocument document = mapper.toDocument(account);

        when(repository.findById("acc-1"))
                .thenReturn(Mono.just(document));

        adapter.findById("acc-1")
                .test()
                .assertComplete()
                .assertValue(found -> found.id().equals("acc-1"));
    }

    @Test
    void shouldFindAllAccounts() {
        AccountDocument document = mapper.toDocument(account());

        when(repository.findAll())
                .thenReturn(Flux.just(document));

        adapter.findAll()
                .test()
                .assertValueCount(1)
                .assertComplete();
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