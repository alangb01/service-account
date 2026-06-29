package pe.nom.charlygastelo.app.accountservice.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountBusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountLimitExceededException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerHasOverdueDebtException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerInactiveException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;

@RequiredArgsConstructor
@Slf4j
public class CreateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final CustomerEventPort customerEventPort;
    private final CreditEventPort creditEventPort;
    private final AccountCachePort cache;

    public Single<Account> execute(Account account) {
        log.info("Starting account creation. customerId={}, type={}, number={}",
                account.customerId(), account.type(), account.number());

        return validateCustomer(account.customerId())
                .flatMap(customer ->
                        validateNoOverdueDebt(customer.id())
                                .andThen(validateOpeningAmount(account))
                                .andThen(validateAccountCreation(customer, account))
                )
                .map(this::prepareNewAccount)
                .flatMap(accountRepository::save)
                .flatMap(saved ->
                        // defensivo: si cache.save devuelve null, no romper el flujo
                        safeCacheSave(saved)
                                .andThen(
                                        producer.publishAccountCreated(saved)
                                                .doOnComplete(() ->
                                                        log.info("AccountCreatedEvent published. accountId={}",
                                                                saved.id()))
                                )
                                .andThen(Single.just(saved))
                )
                .doOnSuccess(saved ->
                        log.info("Account created successfully. accountId={}, customerId={}",
                                saved.id(), saved.customerId()))
                .doOnError(error ->
                        log.error("Error creating account. customerId={}, reason={}",
                                account.customerId(), error.getMessage(), error));
    }

    private Completable safeCacheSave(Account saved) {
        Completable op = cache.save(saved);
        if (op == null) {
            log.warn("AccountCachePort.save returned null. Skipping cache save. accountId={}", saved.id());
            return Completable.complete();
        }
        return op.onErrorComplete(error -> {
            log.warn("Redis save failed after account creation. accountId={}, reason={}",
                    saved.id(), error.getMessage());
            return true;
        });
    }

    private Single<Customer> validateCustomer(String customerId) {
        log.info("Validating customer before account creation. customerId={}", customerId);

        return customerEventPort.getById(customerId)
                .flatMap(customer -> {
                    if (!customer.active()) {
                        return Single.error(
                                new CustomerInactiveException("Customer is inactive")
                        );
                    }

                    return Single.just(customer);
                });
    }

    private Completable validateNoOverdueDebt(String customerId) {
        log.info("Validating overdue debt. customerId={}", customerId);

        return creditEventPort.hasOverdueDebt(customerId)
                .flatMapCompletable(hasDebt -> {
                    if (hasDebt) {
                        return Completable.error(
                                new CustomerHasOverdueDebtException(customerId)
                        );
                    }

                    return Completable.complete();
                });
    }

    private Single<Account> validateAccountCreation(
            Customer customer,
            Account account) {

        log.info("Validating account rules. customerId={}, customerType={}, accountType={}",
                customer.id(), customer.customerType(), account.type());

        if (account.type() == AccountType.VIP_SAVINGS) {
            return validateVipRequirements(customer).andThen(Single.just(account));
        }

        if (account.type() == AccountType.PYME_CHECKING) {
            return validatePymeRequirements(customer).andThen(Single.just(account));
        }

        if (customer.isBusiness()) {
            return validateBusinessAccount(account);
        }

        return validatePersonalAccount(customer, account);
    }

    private Single<Account> validateBusinessAccount(Account account) {

        AccountType type = account.type();

        // Tipos prohibidos para empresas
        if (type == AccountType.SAVINGS ||
                type == AccountType.FIXED_TERM) {

            return Single.error(
                    new AccountBusinessException(
                            "Business customers cannot own savings or fixed-term accounts"
                    )
            );
        }

        // CURRENT y PYME_CURRENT están permitidos (múltiples)
        return Single.just(account);
    }

    private Single<Account> validatePersonalAccount(
            Customer customer,
            Account account) {

        // can have more than one account
        if (account.type() == AccountType.FIXED_TERM) {
            return Single.just(account);
        }


        return accountRepository
                .findByCustomerIdAndType(customer.id(), account.type().name())
                .isEmpty()
                .flatMap(isEmpty -> {
                    if (!isEmpty) {
                        String message = account.type() == AccountType.VIP_SAVINGS
                                ? "Customer already has a VIP savings account"
                                : "Customer already has a " + account.type();
                        return Single.error(new AccountLimitExceededException(message));
                    }

                    return Single.just(account);
                });
    }

    private Completable validateOpeningAmount(Account account) {

        BigDecimal minimum = minimumOpeningAmount(account.type());

        log.info("Validating opening amount. accountType={}, requiredMin={}, provided={}",
                account.type(), minimum, account.balance());

        BigDecimal balance = account.balance() == null
                ? BigDecimal.ZERO
                : account.balance();

        if (balance.compareTo(minimum) < 0) {
            return Completable.error(new AccountBusinessException(
                    "Opening amount is below the required minimum for account type " + account.type()
            ));
        }

        return Completable.complete();
    }

    private BigDecimal minimumOpeningAmount(AccountType type) {

        return switch (type) {
            case SAVINGS,
                 CHECKING,
                 PYME_CHECKING -> BigDecimal.ZERO;

            case FIXED_TERM -> BigDecimal.valueOf(500);

            case VIP_SAVINGS -> BigDecimal.valueOf(1000);
        };
    }

    private Completable validateVipRequirements(Customer customer) {
        log.info("Validating VIP requirements. customerId={}", customer.id());

        if (!customer.isPersonal()) {
            return Completable.error(new AccountBusinessException("VIP accounts are only for personal customers"));
        }

        return creditEventPort.hasActiveCreditCard(customer.id())
                .flatMapCompletable(hasCard -> {
                    if (!hasCard) {
                        return Completable.error(new AccountBusinessException(
                                "VIP account requires an active credit card"
                        ));
                    }
                    return Completable.complete();
                });
    }

    private Completable validatePymeRequirements(Customer customer) {
        log.info("Validating PYME requirements. customerId={}", customer.id());

        if (!customer.isBusiness()) {
            return Completable.error(new AccountBusinessException("PYME accounts are only for business customers"));
        }

        return creditEventPort.hasActiveCreditCard(customer.id())
                .flatMapCompletable(hasCard -> {
                    if (!hasCard) {
                        return Completable.error(new AccountBusinessException(
                                "PYME account requires an active credit card"
                        ));
                    }
                    return Completable.complete();
                });
    }


    private Account prepareNewAccount(Account account) {
        LocalDateTime now = LocalDateTime.now();

        return new Account(
                account.id(),
                account.customerId(),
                account.customerType(),
                account.number(),
                account.type(),
                account.balance(),
                account.currency(),
                account.createdAt() == null ? now : account.createdAt(),
                now,
                null,
                true,
                AccountStatus.ACTIVE
        );
    }
}