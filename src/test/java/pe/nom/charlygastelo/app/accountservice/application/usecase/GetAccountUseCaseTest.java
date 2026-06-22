package pe.nom.charlygastelo.app.accountservice.application.usecase;

import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAccountUseCaseTest {
    private final AccountServicePort accountServicePort = mock(AccountServicePort.class);
    private final GetAccountUseCase getAccountUseCase = new GetAccountUseCase(accountServicePort);

    @Test
    void byIdShouldReturnAccountWhenExists() {
        String accountId = "account-001";

        Account account = new Account(
                accountId,
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountServicePort.getById(accountId)).thenReturn(Mono.just(account));

        StepVerifier.create(getAccountUseCase.byId(accountId))
                .expectNext(account)
                .verifyComplete();

        verify(accountServicePort).getById(accountId);
    }

    @Test
    void byIdShouldReturnEmptyWhenAccountDoesNotExist() {
        String accountId = "account-999";

        when(accountServicePort.getById(accountId)).thenReturn(Mono.empty());

        StepVerifier.create(getAccountUseCase.byId(accountId))
                .verifyComplete();

        verify(accountServicePort).getById(accountId);
    }

    @Test
    void byNumberShouldReturnAccountWhenExists() {
        String accountNumber = "ACC-001";

        Account account = new Account(
                "account-001",
                "customer-001",
                CustomerType.PERSONAL,
                accountNumber,
                AccountType.SAVINGS,
                BigDecimal.valueOf(500),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountServicePort.getByNumber(accountNumber)).thenReturn(Mono.just(account));

        StepVerifier.create(getAccountUseCase.byNumber(accountNumber))
                .expectNext(account)
                .verifyComplete();

        verify(accountServicePort).getByNumber(accountNumber);
    }

    @Test
    void byNumberShouldReturnEmptyWhenAccountDoesNotExist() {
        String accountNumber = "ACC-999";

        when(accountServicePort.getByNumber(accountNumber)).thenReturn(Mono.empty());

        StepVerifier.create(getAccountUseCase.byNumber(accountNumber))
                .verifyComplete();

        verify(accountServicePort).getByNumber(accountNumber);
    }
}
