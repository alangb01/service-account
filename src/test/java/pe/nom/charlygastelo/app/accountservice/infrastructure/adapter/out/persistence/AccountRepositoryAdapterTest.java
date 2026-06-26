package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper.AccountPersistentMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountRepositoryAdapterTest {

    private final ReactiveAccountRepository repository = mock(ReactiveAccountRepository.class);
    private final AccountPersistentMapper mapper = mock(AccountPersistentMapper.class);
    private final AccountRepositoryAdapter adapter = new AccountRepositoryAdapter(repository, mapper);

    @Test
    void saveShouldPersistDocumentAndReturnDomainAccount() {
        LocalDateTime createdAt = LocalDateTime.now();

        Account account = new Account(
                null,
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                null,
                null,
                true,
                "ACTIVE"
        );

        AccountDocument savedDocument = new AccountDocument();
        savedDocument.setId("account-001");
        savedDocument.setCustomerId("customer-001");
        savedDocument.setNumber("ACC-001");
        savedDocument.setType("SAVINGS");
        savedDocument.setBalance(BigDecimal.valueOf(1000));
        savedDocument.setCurrency("PEN");
        savedDocument.setCreatedAt(createdAt);
        savedDocument.setActive(true);
        savedDocument.setStatus("ACTIVE");

        Account domainAccount = new Account(
                "account-001",
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                null,
                null,
                true,
                "ACTIVE"
        );

        when(mapper.toDocument(any(Account.class))).thenReturn(savedDocument);
        when(repository.save(any())).thenReturn(Mono.just(savedDocument));
        when(mapper.toDomain(savedDocument)).thenReturn(domainAccount);

        TestObserver<Account> observer = adapter.save(account).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(domainAccount);

        verify(mapper).toDocument(any(Account.class));
        verify(repository).save(any());
        verify(mapper).toDomain(savedDocument);
    }

    @Test
    void findByIdShouldReturnDomainAccountWhenDocumentExists() {
        String id = "account-001";
        LocalDateTime createdAt = LocalDateTime.now();

        AccountDocument document = new AccountDocument();
        document.setId(id);
        document.setCustomerId("customer-001");
        document.setNumber("ACC-001");
        document.setType("SAVINGS");
        document.setBalance(BigDecimal.valueOf(1000));
        document.setCurrency("PEN");
        document.setCreatedAt(createdAt);
        document.setActive(true);
        document.setStatus("ACTIVE");

        Account account = new Account(
                id,
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                null,
                null,
                true,
                "ACTIVE"
        );

        when(repository.findById(id)).thenReturn(Mono.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestObserver<Account> observer = adapter.findById(id).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(account);

        verify(repository).findById(id);
        verify(mapper).toDomain(document);
    }

    @Test
    void findByIdShouldReturnEmptyWhenDocumentDoesNotExist() {
        String id = "account-999";

        when(repository.findById(id)).thenReturn(Mono.empty());

        TestObserver<Account> observer = adapter.findById(id).test();

        observer.assertComplete()
                .assertNoErrors();

        verify(repository).findById(id);
    }

    @Test
    void findByNumberShouldReturnDomainAccountWhenDocumentExists() {
        String number = "ACC-001";
        LocalDateTime createdAt = LocalDateTime.now();

        AccountDocument document = new AccountDocument();
        document.setId("account-001");
        document.setCustomerId("customer-001");
        document.setNumber(number);
        document.setType("SAVINGS");
        document.setBalance(BigDecimal.valueOf(1000));
        document.setCurrency("PEN");
        document.setCreatedAt(createdAt);
        document.setActive(true);
        document.setStatus("ACTIVE");

        Account account = new Account(
                "account-001",
                "customer-001",
                number,
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                null,
                null,
                true,
                "ACTIVE"
        );

        when(repository.findByNumber(number)).thenReturn(Mono.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestObserver<Account> observer = adapter.findByNumber(number).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(account);

        verify(repository).findByNumber(number);
        verify(mapper).toDomain(document);
    }

    @Test
    void findByCustomerIdShouldReturnDomainAccounts() {
        String customerId = "customer-001";

        AccountDocument firstDocument = new AccountDocument();
        firstDocument.setId("account-001");
        firstDocument.setCustomerId(customerId);
        firstDocument.setNumber("ACC-001");
        firstDocument.setType("SAVINGS");
        firstDocument.setBalance(BigDecimal.valueOf(100));
        firstDocument.setCurrency("PEN");
        firstDocument.setCreatedAt(LocalDateTime.now());
        firstDocument.setActive(true);
        firstDocument.setStatus("ACTIVE");

        AccountDocument secondDocument = new AccountDocument();
        secondDocument.setId("account-002");
        secondDocument.setCustomerId(customerId);
        secondDocument.setNumber("ACC-002");
        secondDocument.setType("CHECKING");
        secondDocument.setBalance(BigDecimal.valueOf(200));
        secondDocument.setCurrency("USD");
        secondDocument.setCreatedAt(LocalDateTime.now());
        secondDocument.setActive(true);
        secondDocument.setStatus("ACTIVE");

        Account firstAccount = new Account("account-001", customerId, "ACC-001", AccountType.SAVINGS,
                BigDecimal.valueOf(100), "PEN", LocalDateTime.now(), null, null, true, "ACTIVE");
        Account secondAccount = new Account("account-002", customerId, "ACC-002", AccountType.CHECKING,
                BigDecimal.valueOf(200), "USD", LocalDateTime.now(), null, null, true, "ACTIVE");

        when(repository.findByCustomerId(customerId)).thenReturn(Flux.just(firstDocument, secondDocument));
        when(mapper.toDomain(firstDocument)).thenReturn(firstAccount);
        when(mapper.toDomain(secondDocument)).thenReturn(secondAccount);

        TestSubscriber<Account> subscriber = adapter.findByCustomerId(customerId).test();

        subscriber.assertComplete()
                .assertNoErrors()
                .assertValues(firstAccount, secondAccount);

        verify(repository).findByCustomerId(customerId);
    }

    @Test
    void findAllShouldReturnAllDomainAccounts() {
        AccountDocument firstDocument = new AccountDocument();
        firstDocument.setId("account-001");
        firstDocument.setCustomerId("customer-001");
        firstDocument.setNumber("ACC-001");
        firstDocument.setType("SAVINGS");
        firstDocument.setBalance(BigDecimal.valueOf(100));
        firstDocument.setCurrency("PEN");
        firstDocument.setCreatedAt(LocalDateTime.now());
        firstDocument.setActive(true);
        firstDocument.setStatus("ACTIVE");

        AccountDocument secondDocument = new AccountDocument();
        secondDocument.setId("account-002");
        secondDocument.setCustomerId("customer-002");
        secondDocument.setNumber("ACC-002");
        secondDocument.setType("SAVINGS");
        secondDocument.setBalance(BigDecimal.valueOf(200));
        secondDocument.setCurrency("USD");
        secondDocument.setCreatedAt(LocalDateTime.now());
        secondDocument.setActive(true);
        secondDocument.setStatus("ACTIVE");

        Account firstAccount = new Account("account-001", "customer-001", "ACC-001", AccountType.SAVINGS,
                BigDecimal.valueOf(100), "PEN", LocalDateTime.now(), null, null, true, "ACTIVE");
        Account secondAccount = new Account("account-002", "customer-002", "ACC-002", AccountType.SAVINGS,
                BigDecimal.valueOf(200), "USD", LocalDateTime.now(), null, null, true, "ACTIVE");

        when(repository.findAll()).thenReturn(Flux.just(firstDocument, secondDocument));
        when(mapper.toDomain(firstDocument)).thenReturn(firstAccount);
        when(mapper.toDomain(secondDocument)).thenReturn(secondAccount);

        TestSubscriber<Account> subscriber = adapter.findAll().test();

        subscriber.assertComplete()
                .assertNoErrors()
                .assertValues(firstAccount, secondAccount);

        verify(repository).findAll();
    }

    @Test
    void deleteByIdShouldDeleteDocument() {
        String id = "account-001";

        when(repository.deleteById(id)).thenReturn(Mono.empty());

        adapter.deleteById(id).test()
                .assertComplete()
                .assertNoErrors();

        verify(repository).deleteById(id);
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(repository.findAll()).thenReturn(Flux.error(new RuntimeException("Mongo error")));

        TestSubscriber<Account> subscriber = adapter.findAll().test();

        subscriber.assertError(e -> e instanceof RuntimeException && e.getMessage().equals("Mongo error"));

        verify(repository).findAll();
    }
}
