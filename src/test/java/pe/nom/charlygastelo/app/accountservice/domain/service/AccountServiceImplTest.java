package pe.nom.charlygastelo.app.accountservice.domain.service;

import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.CustomerClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceImplTest {

    private final AccountRepositoryPort repository = mock(AccountRepositoryPort.class);
    private final CustomerClient customerClient = mock(CustomerClient.class);
    private final AccountServiceImpl service = new AccountServiceImpl(repository, customerClient);

    @Test
    void createShouldSaveAccountSuccessfully() {
        Account accountToSave = account(null, "customer-001", "ACC-001", BigDecimal.valueOf(1000), "PEN");
        Account savedAccount = account("account-001", "customer-001", "ACC-001", BigDecimal.valueOf(1000), "PEN");

        when(repository.findByCustomerId("customer-001")).thenReturn(Flux.empty());
        when(repository.save(accountToSave)).thenReturn(Mono.just(savedAccount));

        StepVerifier.create(service.create(accountToSave))
                .expectNext(savedAccount)
                .verifyComplete();

        verify(repository).save(accountToSave);
    }

    @Test
    void createShouldPropagateRepositoryError() {
        Account account = account(null, "customer-001", "ACC-001", BigDecimal.valueOf(1000), "PEN");

        when(repository.findByCustomerId("customer-001")).thenReturn(Flux.empty());
        when(repository.save(account)).thenReturn(Mono.error(new RuntimeException("Repository error")));

        StepVerifier.create(service.create(account))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Repository error")
                )
                .verify();

        verify(repository).save(account);
    }

    @Test
    void getByIdShouldReturnAccountWhenExists() {
        String id = "account-001";
        Account account = account(id, "customer-001", "ACC-001", BigDecimal.valueOf(1000), "PEN");

        when(repository.findById(id)).thenReturn(Mono.just(account));

        StepVerifier.create(service.getById(id))
                .expectNext(account)
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void getByIdShouldReturnEmptyWhenAccountDoesNotExist() {
        String id = "account-999";

        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.getById(id))
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void getByNumberShouldReturnAccountWhenExists() {
        String number = "ACC-001";
        Account account = account("account-001", "customer-001", number, BigDecimal.valueOf(1000), "PEN");

        when(repository.findByNumber(number)).thenReturn(Mono.just(account));

        StepVerifier.create(service.getByNumber(number))
                .expectNext(account)
                .verifyComplete();

        verify(repository).findByNumber(number);
    }

    @Test
    void getByCustomerShouldReturnCustomerAccounts() {
        String customerId = "customer-001";
        Account firstAccount = account("account-001", customerId, "ACC-001", BigDecimal.valueOf(100), "PEN");
        Account secondAccount = account("account-002", customerId, "ACC-002", BigDecimal.valueOf(200), "USD");

        when(repository.findByCustomerId(customerId)).thenReturn(Flux.just(firstAccount, secondAccount));

        StepVerifier.create(service.getByCustomer(customerId))
                .expectNext(firstAccount)
                .expectNext(secondAccount)
                .verifyComplete();

        verify(repository).findByCustomerId(customerId);
    }

    @Test
    void getAllShouldReturnAllAccounts() {
        Account firstAccount = account("account-001", "customer-001", "ACC-001", BigDecimal.valueOf(100), "PEN");
        Account secondAccount = account("account-002", "customer-002", "ACC-002", BigDecimal.valueOf(200), "USD");

        when(repository.findAll()).thenReturn(Flux.just(firstAccount, secondAccount));

        StepVerifier.create(service.getAll())
                .expectNext(firstAccount)
                .expectNext(secondAccount)
                .verifyComplete();

        verify(repository).findAll();
    }

    private Account account(String id, String customerId, String number, BigDecimal balance, String currency) {
        return new Account(
                id,
                customerId,
                CustomerType.PERSONAL,
                number,
                AccountType.SAVINGS,
                balance,
                currency,
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );
    }

    @Test
    void updateShouldSaveUpdatedAccountSuccessfully() {
        String id = "account-001";
        Account existingAccount = account(id, "customer-001", "ACC-001", BigDecimal.valueOf(1000), "PEN");
        Account accountToUpdate = account(id, "customer-001", "ACC-001", BigDecimal.valueOf(1500), "PEN");
        Account updatedAccount = account(id, "customer-001", "ACC-001", BigDecimal.valueOf(1500), "PEN");

        when(repository.findById(id)).thenReturn(Mono.just(existingAccount));
        when(repository.save(accountToUpdate)).thenReturn(Mono.just(updatedAccount));

        StepVerifier.create(service.update(id, accountToUpdate))
                .expectNext(updatedAccount)
                .verifyComplete();

        verify(repository).findById(id);
        verify(repository).save(accountToUpdate);
    }

    @Test
    void deleteShouldDeactivateAccountSuccessfully() {
        String id = "account-001";
        Account existingAccount = account(id, "customer-001", "ACC-001", BigDecimal.valueOf(1000), "PEN");

        Account inactiveAccount = new Account(
                id,
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                existingAccount.createdAt(),
                false,
                "INACTIVE"
        );

        when(repository.findById(id)).thenReturn(Mono.just(existingAccount));
        when(repository.save(inactiveAccount)).thenReturn(Mono.just(inactiveAccount));

        StepVerifier.create(service.delete(id))
                .expectNext(inactiveAccount)
                .verifyComplete();

        verify(repository).findById(id);
        verify(repository).save(inactiveAccount);
    }
}