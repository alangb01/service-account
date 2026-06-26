package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.CustomerEventPort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateAccountUseCaseTest {
    private final AccountRepositoryPort accountRepository = mock(AccountRepositoryPort.class);
    private final AccountEventProducerPort accountEventProducer = mock(AccountEventProducerPort.class);
    private final CustomerEventPort customerClient = mock(CustomerEventPort.class);
    private final CreateAccountUseCase createAccountUseCase = new CreateAccountUseCase(accountRepository, accountEventProducer, customerClient);

    @Test
    void executeShouldCreateAccountSuccessfully() {
        String customerId = "customer-001";
        
        Customer customer = new Customer(
                customerId, 
                "PERSONAL", 
                "DNI", 
                "12345678", 
                "John", 
                "Doe", 
                "john@example.com", 
                "1234567890", 
                true
        );
        
        Account accountToCreate = new Account(
                null,
                customerId,
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
                customerId,
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

        when(customerClient.getById(customerId)).thenReturn(Single.just(customer));
        when(accountRepository.findByCustomerIdAndType(customerId, "SAVINGS"))
                .thenReturn(Flowable.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(Single.just(createdAccount));
        when(accountEventProducer.publishAccountCreated(any(Account.class))).thenReturn(Completable.complete());

        TestObserver<Account> observer = createAccountUseCase.execute(accountToCreate).test();

        observer.assertComplete()
                .assertNoErrors()
                .assertValue(createdAccount);

        verify(customerClient).getById(customerId);
        verify(accountRepository).findByCustomerIdAndType(customerId, "SAVINGS");
        verify(accountRepository).save(any(Account.class));
        verify(accountEventProducer).publishAccountCreated(any(Account.class));
    }

    @Test
    void executeShouldReturnErrorWhenServiceFails() {
        String customerId = "customer-001";
        
        Customer customer = new Customer(
                customerId, 
                "PERSONAL", 
                "DNI", 
                "12345678", 
                "John", 
                "Doe", 
                "john@example.com", 
                "1234567890", 
                true
        );
        
        Account accountToCreate = new Account(
                null,
                customerId,
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

        RuntimeException exception = new RuntimeException("Error creating account");

        when(customerClient.getById(customerId)).thenReturn(Single.just(customer));
        when(accountRepository.findByCustomerIdAndType(customerId, "SAVINGS"))
                .thenReturn(Flowable.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(Single.error(exception));

        TestObserver<Account> observer = createAccountUseCase.execute(accountToCreate).test();

        observer.assertError(exception);
        verify(customerClient).getById(customerId);
        verify(accountRepository).findByCustomerIdAndType(customerId, "SAVINGS");
        verify(accountRepository).save(any(Account.class));
    }
}
