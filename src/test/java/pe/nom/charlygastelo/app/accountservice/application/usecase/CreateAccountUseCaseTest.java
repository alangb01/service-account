package pe.nom.charlygastelo.app.accountservice.application.usecase;

import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.CustomerClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.service.AccountDomainService;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.CustomerClient;
import pe.nom.charlygastelo.app.accountservice.infrastructure.events.AccountEventProducer;
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
    private final AccountRepositoryPort accountRepository= mock(AccountRepositoryPort.class);
    private final AccountDomainService accountDomainService = mock(AccountDomainService.class);
    private final AccountEventProducer accountEventProducer= mock(AccountEventProducer.class);
    private final CustomerClientPort customerClient = mock(CustomerClient.class);
    private final CreateAccountUseCase createAccountUseCase = new CreateAccountUseCase(accountRepository,accountDomainService,accountEventProducer,customerClient);

    @Test
    void executeShouldCreateAccountSuccessfully() {
        Account accountToCreate = new Account(
                null,
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                null,
                null,
                null,
                true,
                null
        );

        Account createdAccount = new Account(
                "account-001",
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(accountRepository.save(accountToCreate)).thenReturn(Mono.just(createdAccount));

        StepVerifier.create(createAccountUseCase.execute(accountToCreate))
                .expectNext(createdAccount)
                .verifyComplete();

        verify(accountRepository).save(accountToCreate);
    }

    @Test
    void executeShouldReturnErrorWhenServiceFails() {
        Account accountToCreate = new Account(
                null,
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                null,
                null,
                null,
                true,
                "ACTIVE"
        );

        RuntimeException exception = new RuntimeException("Error creating account");

        when(accountRepository.save(accountToCreate)).thenReturn(Mono.error(exception));

        StepVerifier.create(createAccountUseCase.execute(accountToCreate))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                        error.getMessage().equals("Error creating account")
                )
                .verify();

        verify(accountRepository).save(accountToCreate);
    }

    private Account account(String id, String customerId, String number, BigDecimal balance, String currency) {
        return new Account(
                id,
                customerId,
                number,
                AccountType.SAVINGS,
                balance,
                currency,
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );
    }

}
