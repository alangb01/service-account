package pe.nom.charlygastelo.app.accountservice.application.usecase.account;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountBusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountLimitExceededException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerHasOverdueDebtException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CardClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CreditClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CustomerClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.CreateAccountUseCasePort;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAccountUseCaseService implements CreateAccountUseCasePort {

    private final AccountRepositoryPort accountRepository;
    private final CustomerClientPort customerClient;
    private final CreditClientPort creditClient;
    private final CardClientPort cardClient;
    private final AccountEventProducerPort eventProducer;

    @Override
    public Single<Account> create(Account account, String token) {

        log.info("[ACCOUNT CREATE] Starting account creation. customerId={}, type={}",
                account.customerId(), account.type());

        return customerClient.getById(account.customerId(), token)
                .flatMap(customer -> validateCustomer(customer, account)
                        .andThen(validateAccountRules(customer, account))
                        .andThen(validateNoOverdueDebt(customer.id(), token))
                        .andThen(validateSpecialProfiles(customer, account, token))
                        .andThen(validateOpeningAmount(account))
                        .andThen(checkAccountLimits(customer, account))
                        .andThen(Single.just(customer))
                )
                .flatMap(customer -> {
                    Account newAccount = account.createWith(account);
                    newAccount = newAccount.withCreatedAt(Instant.now());

                    log.info("[ACCOUNT CREATE] Saving account. customerId={}, number={}",
                            account.customerId(), account.number());

                    return accountRepository.save(newAccount);
                })
                .flatMap(saved -> {
                    log.info("[ACCOUNT CREATE] Account saved successfully. accountId={} balance={}", saved.id(),saved.balance().doubleValue());



                    return eventProducer.publishAccountCreated(saved)
                            .andThen(eventProducer.publishAccountInitialDeposit(saved))
                            .andThen(Single.just(saved));
                })
                .doOnSuccess(acc ->
                        log.info("[ACCOUNT CREATE] Process completed successfully. accountId={}", acc.id()))
                .doOnError(err ->
                        log.error("[ACCOUNT CREATE] Error creating account. customerId={}, reason={}",
                                account.customerId(), err.getMessage(), err));
    }

    // -------------------------------------------------------------
    // VALIDACIONES
    // -------------------------------------------------------------

    private Completable validateCustomer(Customer customer, Account account) {
        if (!customer.active()) {
            return Completable.error(new AccountBusinessException("Customer is inactive"));
        }
        return Completable.complete();
    }

    private Completable validateAccountRules(Customer customer, Account account) {
        return switch (customer.customerType()) {
            case PERSONAL -> validatePersonalAccount(customer, account);
            case BUSINESS -> validateBusinessAccount(customer, account);
            default -> Completable.error(new AccountBusinessException("Invalid customer type"));
        };
    }

    private Completable validatePersonalAccount(Customer customer, Account account) {

        switch (account.type()) {
            case SAVINGS -> {
                return accountRepository
                        .findByCustomerIdAndType(customer.id(), "SAVINGS")
                        .isEmpty()
                        .flatMapCompletable(isEmpty -> {
                            if (!isEmpty) {
                                return Completable.error(new AccountLimitExceededException(
                                        "Customer already has a savings account"));
                            }
                            return Completable.complete();
                        });
            }

            case CHECKING -> {
                return accountRepository
                        .findByCustomerIdAndType(customer.id(), "CHECKING")
                        .isEmpty()
                        .flatMapCompletable(isEmpty -> {
                            if (!isEmpty) {
                                return Completable.error(new AccountLimitExceededException(
                                        "Customer already has a checking account"));
                            }
                            return Completable.complete();
                        });
            }

            case FIXED_TERM -> {
                return Completable.complete(); // puede tener múltiples
            }

            case VIP_SAVINGS -> {
                return accountRepository
                        .findByCustomerIdAndType(customer.id(), "VIP_SAVINGS")
                        .isEmpty()
                        .flatMapCompletable(isEmpty -> {
                            if (!isEmpty) {
                                return Completable.error(new AccountLimitExceededException(
                                        "Customer already has a VIP savings account"));
                            }
                            return Completable.complete();
                        });
            }

            default -> {
                return Completable.error(new AccountBusinessException("Invalid account type for personal customer"));
            }
        }
    }

    private Completable validateBusinessAccount(Customer customer, Account account) {

        switch (account.type()) {
            case SAVINGS, FIXED_TERM -> {
                return Completable.error(new AccountBusinessException(
                        "Business customers cannot own savings or fixed-term accounts"));
            }

            case CHECKING, PYME_CHECKING -> {
                return Completable.complete(); // múltiples permitidas
            }

            default -> {
                return Completable.error(new AccountBusinessException("Invalid account type for business customer"));
            }
        }
    }

    private Completable validateNoOverdueDebt(String customerId, String token) {
        return creditClient.hasOverdueDebt(customerId, token)
                .flatMapCompletable(hasDebt -> {
                    if (hasDebt) {
                        return Completable.error(new CustomerHasOverdueDebtException(customerId));
                    }
                    return Completable.complete();
                });
    }

    private Completable validateSpecialProfiles(Customer customer, Account account, String token) {

        if (account.type()==AccountType.PYME_CHECKING || account.type()==AccountType.VIP_SAVINGS) {
            return cardClient.hasActiveCreditCard(customer.id(), token)
                    .flatMapCompletable(hasCard -> {
                        if (!hasCard) {
                            return Completable.error(new AccountBusinessException(
                                    "This account type requires an active credit card"));
                        }
                        return Completable.complete();
                    });
        }

        return Completable.complete();
    }

    private Completable validateOpeningAmount(Account account) {
        BigDecimal balance = account.balance();

        // Validación de nulos
        if (balance == null) {
            return Completable.error(new AccountBusinessException(
                    "Opening amount is required"));
        }

        // Monto mínimo por tipo de cuenta
        BigDecimal min = switch (account.type()) {
            case SAVINGS, CHECKING, PYME_CHECKING -> BigDecimal.ZERO;
            case FIXED_TERM -> new BigDecimal("500");
            case VIP_SAVINGS -> new BigDecimal("1000");
            default -> BigDecimal.ZERO;
        };

        // No permitir montos negativos
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return Completable.error(new AccountBusinessException(
                    "Opening amount cannot be negative"));
        }

        // Validación final
        if (balance.compareTo(min) < 0) {
            return Completable.error(new AccountBusinessException(
                    "Opening amount is below the required minimum"));
        }

        return Completable.complete();
    }

    private Completable checkAccountLimits(Customer customer, Account account) {
        return Completable.complete(); // puedes agregar límites adicionales
    }
}

