package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListAccountsUseCaseTest {

    private final AccountRepositoryPort accountRepository = mock(AccountRepositoryPort.class);
    private final ListAccountsUseCase listAccountsUseCase = new ListAccountsUseCase(accountRepository);

    @Test
    void allShouldReturnAllAccounts() {
        Account firstAccount = new Account(
                "account-001",
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(100),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        Account secondAccount = new Account(
                "account-002",
                "customer-002",
                "ACC-002",
                AccountType.CHECKING,
                BigDecimal.valueOf(200),
                "USD",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(accountRepository.findAll()).thenReturn(Flowable.just(firstAccount, secondAccount));

        TestSubscriber<Account> subscriber = listAccountsUseCase.all().test();

        subscriber.assertComplete()
                .assertNoErrors()
                .assertValues(firstAccount, secondAccount);

        verify(accountRepository).findAll();
    }

    @Test
    void allShouldReturnEmptyWhenThereAreNoAccounts() {
        when(accountRepository.findAll()).thenReturn(Flowable.empty());

        TestSubscriber<Account> subscriber = listAccountsUseCase.all().test();

        subscriber.assertComplete()
                .assertNoErrors();

        verify(accountRepository).findAll();
    }

    @Test
    void byCustomerShouldReturnAccountsForCustomer() {
        String customerId = "customer-001";

        Account firstAccount = new Account(
                "account-001",
                customerId,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(100),
                "PEN",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        Account secondAccount = new Account(
                "account-002",
                customerId,
                "ACC-002",
                AccountType.FIXED_TERM,
                BigDecimal.valueOf(300),
                "USD",
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Flowable.just(firstAccount, secondAccount));

        TestSubscriber<Account> subscriber = listAccountsUseCase.byCustomer(customerId).test();

        subscriber.assertComplete()
                .assertNoErrors()
                .assertValues(firstAccount, secondAccount);

        verify(accountRepository).findByCustomerId(customerId);
    }

    @Test
    void byCustomerShouldReturnEmptyWhenCustomerHasNoAccounts() {
        String customerId = "customer-999";

        when(accountRepository.findByCustomerId(customerId)).thenReturn(Flowable.empty());

        TestSubscriber<Account> subscriber = listAccountsUseCase.byCustomer(customerId).test();

        subscriber.assertComplete()
                .assertNoErrors();

        verify(accountRepository).findByCustomerId(customerId);
    }
}
