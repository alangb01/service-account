package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.domain.exception.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CreateAccountUseCaseTest {

    private AccountRepositoryPort repository;
    private AccountCachePort cache;
    private CustomerEventPort customerEventPort;
    private CreditEventPort creditEventPort;
    private AccountEventProducerPort eventProducer;

    private CreateAccountUseCase useCase;

    @BeforeEach
    void setup() {
        repository = mock(AccountRepositoryPort.class);
        cache = mock(AccountCachePort.class);
        customerEventPort = mock(CustomerEventPort.class);
        creditEventPort = mock(CreditEventPort.class);
        eventProducer = mock(AccountEventProducerPort.class);

        useCase = new CreateAccountUseCase(
                repository,
                eventProducer,
                customerEventPort,
                creditEventPort,
                cache
        );

        when(cache.save(any())).thenReturn(Completable.complete());
        when(eventProducer.publishAccountCreated(any())).thenReturn(Completable.complete());
    }

    // ---------------------------------------------------------
    // PERSONAL: NO PUEDE TENER DOS SAVINGS
    // ---------------------------------------------------------
    @Test
    void shouldFailWhenPersonalCustomerAlreadyHasSavingsAccount() {

        when(customerEventPort.getById("cus-1"))
                .thenReturn(Single.just(personalCustomer()));

        when(creditEventPort.hasOverdueDebt("cus-1"))
                .thenReturn(Single.just(false));

        when(repository.findByCustomerIdAndType("cus-1", "SAVINGS"))
                .thenReturn(Flowable.just(accountSavings()));

        useCase.execute(accountSavings())
                .test()
                .assertError(AccountLimitExceededException.class);
    }

    // ---------------------------------------------------------
    // PERSONAL: DEUDA VENCIDA
    // ---------------------------------------------------------
    @Test
    void shouldFailWhenCustomerHasOverdueDebt() {

        when(customerEventPort.getById("cus-1"))
                .thenReturn(Single.just(personalCustomer()));

        when(creditEventPort.hasOverdueDebt("cus-1"))
                .thenReturn(Single.just(true));

        when(repository.findByCustomerIdAndType(anyString(), anyString()))
                .thenReturn(Flowable.empty());

        useCase.execute(accountSavings())
                .test()
                .assertError(CustomerHasOverdueDebtException.class);
    }

    // ---------------------------------------------------------
    // BUSINESS: NO PUEDE TENER SAVINGS
    // ---------------------------------------------------------
    @Test
    void shouldFailWhenBusinessCustomerCreatesSavingsAccount() {

        when(customerEventPort.getById("cus-1"))
                .thenReturn(Single.just(businessCustomer()));

        when(creditEventPort.hasOverdueDebt("cus-1"))
                .thenReturn(Single.just(false));

        when(repository.findByCustomerIdAndType(anyString(), anyString()))
                .thenReturn(Flowable.empty());

        useCase.execute(accountSavings())
                .test()
                .assertError(AccountBusinessException.class);
    }

    // ---------------------------------------------------------
    // VIP: DEBE TENER TARJETA DE CRÉDITO ACTIVA
    // ---------------------------------------------------------
//    @Test
//    void shouldFailWhenVipSavingsHasNoActiveCreditCard() {
//
//        when(customerEventPort.getById("cus-1"))
//                .thenReturn(Single.just(vipCustomer()));
//
//        when(creditEventPort.hasOverdueDebt("cus-1"))
//                .thenReturn(Single.just(false));
//
//        when(creditEventPort.hasActiveCreditCard("cus-1"))
//                .thenReturn(Single.just(false)); // OBLIGATORIO
//
//        when(repository.findByCustomerIdAndType(anyString(), anyString()))
//                .thenReturn(Flowable.empty()); // OBLIGATORIO
//
//        useCase.execute(accountSavings())
//                .test()
//                .assertError(AccountBusinessException.class);
//    }

    // ---------------------------------------------------------
    // PYME: DEBE TENER TARJETA DE CRÉDITO ACTIVA
    // ---------------------------------------------------------
//    @Test
//    void shouldFailWhenPymeHasNoActiveCreditCard() {
//
//        when(customerEventPort.getById("cus-1"))
//                .thenReturn(Single.just(pymeCustomer()));
//
//        when(creditEventPort.hasOverdueDebt("cus-1"))
//                .thenReturn(Single.just(false));
//
//        when(creditEventPort.hasActiveCreditCard("cus-1"))
//                .thenReturn(Single.just(false)); // OBLIGATORIO
//
//        when(repository.findByCustomerIdAndType(anyString(), anyString()))
//                .thenReturn(Flowable.empty()); // OBLIGATORIO
//
//        useCase.execute(accountSavings())
//                .test()
//                .assertError(AccountBusinessException.class);
//    }

    // ---------------------------------------------------------
    // FACTORIES
    // ---------------------------------------------------------
    private Customer personalCustomer() {
        return new Customer(
                "cus-1",
                "PERSONAL",
                "DNI",
                "12345678",
                "REGULAR",
                "Juan",
                "Pérez",
                "juan@example.com",
                "999999999",
                true
        );
    }

    private Customer businessCustomer() {
        return new Customer(
                "cus-1",
                "BUSINESS",
                "RUC",
                "20123456789",
                "REGULAR",
                "Empresa SAC",
                "Corporation",
                "contacto@empresa.com",
                "987654321",
                true
        );
    }

    private Customer vipCustomer() {
        return new Customer(
                "cus-1",
                "PERSONAL",
                "DNI",
                "12345678",
                "VIP",
                "Juan",
                "Pérez",
                "vip@example.com",
                "999999999",
                true
        );
    }

    private Customer pymeCustomer() {
        return new Customer(
                "cus-1",
                "BUSINESS",
                "RUC",
                "20123456789",
                "PYME",
                "Empresa PYME",
                "SAC",
                "pyme@example.com",
                "987654321",
                true
        );
    }

    private Account accountSavings() {
        return new Account(
                "acc-1",
                "cus-1",
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
