package pe.nom.charlygastelo.app.accountservice.application.usecase;

import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListAccountsUseCaseTest {

    private final AccountServicePort accountServicePort = mock(AccountServicePort.class);
    private final ListAccountsUseCase listAccountsUseCase = new ListAccountsUseCase(accountServicePort);

    @Test
    void allShouldReturnAllAccounts() {
        Account firstAccount = new Account(
                "account-001",
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(100),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        Account secondAccount = new Account(
                "account-002",
                "customer-002",
                CustomerType.BUSINESS,
                "ACC-002",
                AccountType.CHECKING,
                BigDecimal.valueOf(200),
                "USD",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountServicePort.getAll()).thenReturn(Flux.just(firstAccount, secondAccount));

        StepVerifier.create(listAccountsUseCase.all())
                .expectNext(firstAccount)
                .expectNext(secondAccount)
                .verifyComplete();

        verify(accountServicePort).getAll();
    }

    @Test
    void allShouldReturnEmptyWhenThereAreNoAccounts() {
        when(accountServicePort.getAll()).thenReturn(Flux.empty());

        StepVerifier.create(listAccountsUseCase.all())
                .verifyComplete();

        verify(accountServicePort).getAll();
    }

    @Test
    void byCustomerShouldReturnAccountsForCustomer() {
        String customerId = "customer-001";

        Account firstAccount = new Account(
                "account-001",
                customerId,
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(100),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        Account secondAccount = new Account(
                "account-002",
                customerId,
                CustomerType.PERSONAL,
                "ACC-002",
                AccountType.FIXED_TERM,
                BigDecimal.valueOf(300),
                "USD",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountServicePort.getByCustomer(customerId)).thenReturn(Flux.just(firstAccount, secondAccount));

        StepVerifier.create(listAccountsUseCase.byCustomer(customerId))
                .expectNext(firstAccount)
                .expectNext(secondAccount)
                .verifyComplete();

        verify(accountServicePort).getByCustomer(customerId);
    }

    @Test
    void byCustomerShouldReturnEmptyWhenCustomerHasNoAccounts() {
        String customerId = "customer-999";

        when(accountServicePort.getByCustomer(customerId)).thenReturn(Flux.empty());

        StepVerifier.create(listAccountsUseCase.byCustomer(customerId))
                .verifyComplete();

        verify(accountServicePort).getByCustomer(customerId);
    }
}
