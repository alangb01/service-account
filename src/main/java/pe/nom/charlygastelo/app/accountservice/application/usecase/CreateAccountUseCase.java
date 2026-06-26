package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountBusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountLimitExceededException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerInactiveException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.CustomerEventPort;


@RequiredArgsConstructor
@Slf4j
public class CreateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final CustomerEventPort customerEventPort;

    public Single<Account> execute(Account account) {

        log.info("Starting account creation process for customer {}", account.customerId());

        return validateCustomer(account.customerId())
                // Validar restricciones para crear cuenta
                .flatMap(customer ->validateAccountCreation(customer, account))
                // Guardar cuenta (Single<Account>)
                .flatMap(accountRepository::save)
                .doOnSuccess(saved ->
                        log.info("Customer {} saved successfully with ID {}", saved.number(), saved.id())
                )

                // Publicar evento (Completable → Single<Account>)
                .flatMap(saved ->
                        producer.publishAccountCreated(saved)
                                .doOnComplete(() ->
                                        log.info("AccountCreatedEvent published for {}", saved.number())
                                )
                                .doOnError(e ->
                                        log.error("Error publishing event for {}: {}",
                                                saved.number(), e.getMessage(), e)
                                )
                                .andThen(Single.just(saved))
                ).doOnError(e ->
                        log.error("Error creating account {}: {}", account.number(), e.getMessage(), e)
                );

    }

    private Single<Account> validateBusinessAccount(Customer customer, Account account) {
        log.info("Starting validate business account {} {}", account.customerId(), account.type().toString());

        if (account.type() == AccountType.SAVINGS
                || account.type() == AccountType.FIXED_TERM) {

            return Single.error(
                    new AccountBusinessException(
                            "Business customers cannot own savings or fixed-term accounts"
                    ));
        }
        log.info("fin validate business account {} {}", account.customerId(), account.type().toString());
        return Single.just(account);
    }

    private Single<Account> validatePersonalAccount(Customer customer, Account account) {
        log.info("Starting validate personal account {} {}", account.customerId(), account.type().toString());
        if (account.type() == AccountType.FIXED_TERM) {
            return Single.just(account);
        }

        return accountRepository
                .findByCustomerIdAndType(customer.id(), account.type().toString())
                .isEmpty()
                .flatMap(isEmpty -> {
                    if (!isEmpty) {
                        return Single.error(
                                new AccountLimitExceededException(
                                        "Customer already has a " + account.type()
                                ));
                    }
                    log.info("fin validate business account {} {}", account.customerId(), account.type().toString());
                    return Single.just(account);
                });
    }

    private Single<Account> validateAccountCreation(Customer customer, Account account) {
        log.info("Validating account creation for customer {}", customer.id());
        if (customer.isBusiness()) {
            return validateBusinessAccount(customer, account);
        }

        return validatePersonalAccount(customer, account);
    }

    private Single<Customer> validateCustomer(String customerId) {

        log.info("Validating customer {}", customerId);

        return customerEventPort.getById(customerId)
                .flatMap(customer -> {
                    System.out.println(customer);
                    if (!customer.active()) {
                        return Single.error(
                                new CustomerInactiveException("Customer is inactive")
                        );
                    }
                    return Single.just(customer);
                });
    }

}