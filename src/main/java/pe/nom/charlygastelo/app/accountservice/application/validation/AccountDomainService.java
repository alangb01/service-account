package pe.nom.charlygastelo.app.accountservice.application.validation;

import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessAccountException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerInactiveException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import reactor.core.publisher.Mono;

public class AccountDomainService {
    /**
     * Validates whether a customer is allowed to create or own the given account.
     * This method applies the business rules for personal and business customers.
     *
     * @param customer the customer to validate
     * @param account the account being created or validated
     * @return Mono<Customer> the same customer if validation passes, otherwise an error
     */
    public Mono<Customer> validateCustomer(Customer customer, Account account) {

        // Rule 1: A customer must be active to operate or create accounts.
        if (!customer.active()) {
            return Mono.error(new CustomerInactiveException("Cliente inactivo "+customer.id()));
        }

        // Rule 2: Business customers have different allowed account types.
        if (customer.isBusiness()) {
            // Validate business account rules and return the customer if valid.
            return validateBusinessCustomer(account).thenReturn(customer);
        }

        // Rule 3: Personal customers have their own account restrictions.
        return validatePersonalCustomer(account).thenReturn(customer);
    }

    /**
     * Validates business customer rules.
     * Business customers:
     *  - Cannot have savings accounts.
     *  - Cannot have fixed-term accounts.
     *  - Can only have one or more checking accounts.
     *
     * @param account the account being created
     * @return Mono<Void> completes if valid, otherwise emits an error
     */
    private Mono<Void> validateBusinessCustomer(Account account) {

        // Business customers are restricted to checking accounts only.
        if (account.type().isSavings() || account.type().isFixedTerm()) {
            return Mono.error(new BusinessAccountException(
                    "Empresas solo puede tener una o mas cuentas corrientes"
            ));
        }

        // If the account type is valid, return an empty Mono to continue the chain.
        return Mono.empty();
    }

    /**
     * Validates personal customer rules.
     * Personal customers:
     *  - Can have only one savings account.
     *  - Can have only one checking account.
     *  - Can have multiple fixed-term accounts.
     *
     * NOTE: This method currently does not enforce limits.
     *       The repository-level validation should be added here.
     *
     * @param account the account being created
     * @return Mono<Void> completes if valid, otherwise emits an error
     */
    private Mono<Void> validatePersonalCustomer(Account account) {

        // Fixed-term accounts are always allowed for personal customers.
        if (account.type().isFixedTerm()) {
            return Mono.empty();
        }

        // TODO: Validate limits for savings and checking accounts.
        // Example:
        // return repository.countByCustomerIdAndType(customer.id(), account.type())
        //        .flatMap(count -> count > 0
        //            ? Mono.error(new PersonalAccountLimitException(...))
        //            : Mono.empty());

        return Mono.empty();
    }

}
