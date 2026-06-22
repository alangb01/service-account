package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountRepositoryAdapterTest {

    private final ReactiveAccountRepository repository = mock(ReactiveAccountRepository.class);
    private final AccountRepositoryAdapter adapter = new AccountRepositoryAdapter(repository);

    @Test
    void saveShouldPersistDocumentAndReturnDomainAccount() {
        LocalDateTime createdAt = LocalDateTime.now();

        Account account = new Account(
                null,
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                true,
                "ACTIVE"
        );

        AccountDocument savedDocument = document(
                "account-001",
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                true,
                "ACTIVE"
        );

        when(repository.save(argThat(document ->
                document.getId() == null &&
                        document.getCustomerId().equals("customer-001") &&
                        document.getCustomerType() == CustomerType.PERSONAL &&
                        document.getNumber().equals("ACC-001") &&
                        document.getType() == AccountType.SAVINGS &&
                        document.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0 &&
                        document.getCurrency().equals("PEN") &&
                        document.getCreatedAt().equals(createdAt) &&
                        document.isActive() &&
                        document.getStatus().equals("ACTIVE")
        ))).thenReturn(Mono.just(savedDocument));

        StepVerifier.create(adapter.save(account))
                .expectNextMatches(savedAccount ->
                        savedAccount.id().equals("account-001") &&
                                savedAccount.customerId().equals("customer-001") &&
                                savedAccount.customerType() == CustomerType.PERSONAL &&
                                savedAccount.number().equals("ACC-001") &&
                                savedAccount.type() == AccountType.SAVINGS &&
                                savedAccount.balance().compareTo(BigDecimal.valueOf(1000)) == 0 &&
                                savedAccount.currency().equals("PEN") &&
                                savedAccount.createdAt().equals(createdAt) &&
                                savedAccount.active() &&
                                savedAccount.status().equals("ACTIVE")
                )
                .verifyComplete();

        verify(repository).save(argThat(document ->
                document.getCustomerId().equals("customer-001") &&
                        document.getCustomerType() == CustomerType.PERSONAL &&
                        document.getNumber().equals("ACC-001") &&
                        document.getType() == AccountType.SAVINGS
        ));
    }

    @Test
    void findByIdShouldReturnDomainAccountWhenDocumentExists() {
        String id = "account-001";
        LocalDateTime createdAt = LocalDateTime.now();

        AccountDocument document = document(
                id,
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                true,
                "ACTIVE"
        );

        when(repository.findById(id)).thenReturn(Mono.just(document));

        StepVerifier.create(adapter.findById(id))
                .expectNextMatches(account ->
                        account.id().equals(id) &&
                                account.customerId().equals("customer-001") &&
                                account.customerType() == CustomerType.PERSONAL &&
                                account.number().equals("ACC-001") &&
                                account.type() == AccountType.SAVINGS &&
                                account.balance().compareTo(BigDecimal.valueOf(1000)) == 0 &&
                                account.currency().equals("PEN") &&
                                account.createdAt().equals(createdAt) &&
                                account.active() &&
                                account.status().equals("ACTIVE")
                )
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void findByIdShouldReturnEmptyWhenDocumentDoesNotExist() {
        String id = "account-999";

        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(id))
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void findByNumberShouldReturnDomainAccountWhenDocumentExists() {
        String number = "ACC-001";
        LocalDateTime createdAt = LocalDateTime.now();

        AccountDocument document = document(
                "account-001",
                "customer-001",
                number,
                BigDecimal.valueOf(1000),
                "PEN",
                createdAt,
                true,
                "ACTIVE"
        );

        when(repository.findByNumber(number)).thenReturn(Mono.just(document));

        StepVerifier.create(adapter.findByNumber(number))
                .expectNextMatches(account ->
                        account.id().equals("account-001") &&
                                account.customerId().equals("customer-001") &&
                                account.customerType() == CustomerType.PERSONAL &&
                                account.number().equals(number) &&
                                account.type() == AccountType.SAVINGS &&
                                account.balance().compareTo(BigDecimal.valueOf(1000)) == 0 &&
                                account.currency().equals("PEN") &&
                                account.createdAt().equals(createdAt) &&
                                account.active() &&
                                account.status().equals("ACTIVE")
                )
                .verifyComplete();

        verify(repository).findByNumber(number);
    }

    @Test
    void findByCustomerIdShouldReturnDomainAccounts() {
        String customerId = "customer-001";

        AccountDocument firstDocument = document(
                "account-001",
                customerId,
                "ACC-001",
                BigDecimal.valueOf(100),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        AccountDocument secondDocument = document(
                "account-002",
                customerId,
                "ACC-002",
                BigDecimal.valueOf(200),
                "USD",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(repository.findByCustomerId(customerId)).thenReturn(
                Flux.just(firstDocument, secondDocument)
        );

        StepVerifier.create(adapter.findByCustomerId(customerId))
                .expectNextMatches(account ->
                        account.id().equals("account-001") &&
                                account.customerId().equals(customerId) &&
                                account.customerType() == CustomerType.PERSONAL &&
                                account.number().equals("ACC-001") &&
                                account.type() == AccountType.SAVINGS
                )
                .expectNextMatches(account ->
                        account.id().equals("account-002") &&
                                account.customerId().equals(customerId) &&
                                account.customerType() == CustomerType.PERSONAL &&
                                account.number().equals("ACC-002") &&
                                account.type() == AccountType.SAVINGS
                )
                .verifyComplete();

        verify(repository).findByCustomerId(customerId);
    }

    @Test
    void findAllShouldReturnAllDomainAccounts() {
        AccountDocument firstDocument = document(
                "account-001",
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(100),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        AccountDocument secondDocument = document(
                "account-002",
                "customer-002",
                "ACC-002",
                BigDecimal.valueOf(200),
                "USD",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(repository.findAll()).thenReturn(Flux.just(firstDocument, secondDocument));

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(account ->
                        account.id().equals("account-001") &&
                                account.customerId().equals("customer-001") &&
                                account.customerType() == CustomerType.PERSONAL &&
                                account.number().equals("ACC-001") &&
                                account.type() == AccountType.SAVINGS
                )
                .expectNextMatches(account ->
                        account.id().equals("account-002") &&
                                account.customerId().equals("customer-002") &&
                                account.customerType() == CustomerType.PERSONAL &&
                                account.number().equals("ACC-002") &&
                                account.type() == AccountType.SAVINGS
                )
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void deleteByIdShouldDeleteDocument() {
        String id = "account-001";

        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id))
                .verifyComplete();

        verify(repository).deleteById(id);
    }

    @Test
    void shouldPropagateRepositoryError() {
        when(repository.findAll()).thenReturn(Flux.error(new RuntimeException("Mongo error")));

        StepVerifier.create(adapter.findAll())
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Mongo error")
                )
                .verify();

        verify(repository).findAll();
    }

    private AccountDocument document(
            String id,
            String customerId,
            String number,
            BigDecimal balance,
            String currency,
            LocalDateTime createdAt,
            boolean active,
            String status
    ) {
        AccountDocument document = new AccountDocument();
        document.setId(id);
        document.setCustomerId(customerId);
        document.setCustomerType(CustomerType.PERSONAL);
        document.setNumber(number);
        document.setType(AccountType.SAVINGS);
        document.setBalance(balance);
        document.setCurrency(currency);
        document.setCreatedAt(createdAt);
        document.setActive(active);
        document.setStatus(status);
        return document;
    }
}