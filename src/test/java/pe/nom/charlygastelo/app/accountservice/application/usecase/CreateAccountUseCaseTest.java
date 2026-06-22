package pe.nom.charlygastelo.app.accountservice.application.usecase;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateAccountUseCaseTest {
    private final AccountServicePort accountServicePort = mock(AccountServicePort.class);
    private final CreateAccountUseCase createAccountUseCase = new CreateAccountUseCase(accountServicePort);

    @Test
    void executeShouldCreateAccountSuccessfully() {
        Account accountToCreate = new Account(
                null,
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                null,
                true,
                "ACTIVE"
        );

        Account createdAccount = new Account(
                "account-001",
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountServicePort.create(accountToCreate)).thenReturn(Mono.just(createdAccount));

        StepVerifier.create(createAccountUseCase.execute(accountToCreate))
                .expectNext(createdAccount)
                .verifyComplete();

        verify(accountServicePort).create(accountToCreate);
    }

    @Test
    void executeShouldReturnErrorWhenServiceFails() {
        Account accountToCreate = new Account(
                null,
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                null,
                true,
                "ACTIVE"
        );

        RuntimeException exception = new RuntimeException("Error creating account");

        when(accountServicePort.create(accountToCreate)).thenReturn(Mono.error(exception));

        StepVerifier.create(createAccountUseCase.execute(accountToCreate))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                        error.getMessage().equals("Error creating account")
                )
                .verify();

        verify(accountServicePort).create(accountToCreate);
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

}
