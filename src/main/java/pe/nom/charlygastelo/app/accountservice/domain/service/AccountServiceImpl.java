package pe.nom.charlygastelo.app.accountservice.domain.service;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AccountServiceImpl implements AccountServicePort {

    private final AccountRepositoryPort repository;

    public AccountServiceImpl(AccountRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Account> create(Account account) {
        return validateAccountCreation(account)
                .then(repository.save(account));
    }

    @Override
    public Mono<Account> update(String id, Account account) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")))
                .flatMap(existingAccount -> {
                    Account updatedAccount = new Account(
                            existingAccount.id(),
                            account.customerId(),
                            account.customerType(),
                            account.number(),
                            account.type(),
                            account.balance(),
                            account.currency(),
                            existingAccount.createdAt(),
                            account.active(),
                            account.status()
                    );

                    return validateAccountUpdate(existingAccount, updatedAccount)
                            .then(repository.save(updatedAccount));
                });
    }

    @Override
    public Mono<Account> delete(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found")))
                .flatMap(existingAccount -> {
                    Account inactiveAccount = new Account(
                            existingAccount.id(),
                            existingAccount.customerId(),
                            existingAccount.customerType(),
                            existingAccount.number(),
                            existingAccount.type(),
                            existingAccount.balance(),
                            existingAccount.currency(),
                            existingAccount.createdAt(),
                            false,
                            "INACTIVE"
                    );

                    return repository.save(inactiveAccount);
                });
    }

    private Mono<Void> validateAccountCreation(Account account) {
        if (account.customerId() == null || account.customerId().isBlank()) {
            return Mono.error(new IllegalArgumentException("Customer id is required"));
        }

        if (account.customerType() == null) {
            return Mono.error(new IllegalArgumentException("Customer type is required"));
        }

        if (account.type() == null) {
            return Mono.error(new IllegalArgumentException("Account type is required"));
        }

        if (account.customerType() == CustomerType.BUSINESS) {
            return validateBusinessCustomerAccount(account);
        }

        if (account.customerType() == CustomerType.PERSONAL) {
            return validatePersonalCustomerAccount(account);
        }

        return Mono.error(new IllegalArgumentException("Unsupported customer type"));
    }

    private Mono<Void> validateAccountUpdate(Account existingAccount, Account updatedAccount) {
        if (updatedAccount.customerId() == null || updatedAccount.customerId().isBlank()) {
            return Mono.error(new IllegalArgumentException("Customer id is required"));
        }

        if (updatedAccount.customerType() == null) {
            return Mono.error(new IllegalArgumentException("Customer type is required"));
        }

        if (updatedAccount.type() == null) {
            return Mono.error(new IllegalArgumentException("Account type is required"));
        }

        if (!existingAccount.customerId().equals(updatedAccount.customerId())) {
            return Mono.error(new IllegalArgumentException("Customer id cannot be changed"));
        }

        if (existingAccount.type() != updatedAccount.type()) {
            return Mono.error(new IllegalArgumentException("Account type cannot be changed"));
        }

        if (existingAccount.customerType() != updatedAccount.customerType()) {
            return Mono.error(new IllegalArgumentException("Customer type cannot be changed"));
        }

        return Mono.empty();
    }

    private Mono<Void> validateBusinessCustomerAccount(Account account) {
        if (account.type() == AccountType.SAVINGS || account.type() == AccountType.FIXED_TERM) {
            return Mono.error(new IllegalArgumentException(
                    "Business customers can only have checking accounts"
            ));
        }

        return Mono.empty();
    }

    private Mono<Void> validatePersonalCustomerAccount(Account account) {
        if (account.type() == AccountType.FIXED_TERM) {
            return Mono.empty();
        }

        return repository.findByCustomerId(account.customerId())
                .filter(existingAccount -> existingAccount.type() == account.type())
                .hasElements()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new IllegalArgumentException(
                                "Personal customers can have only one account of type " + account.type()
                        ));
                    }

                    return Mono.empty();
                });
    }

    @Override
    public Mono<Account> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Account> getByNumber(String number) {
        return repository.findByNumber(number);
    }

    @Override
    public Flux<Account> getByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public Flux<Account> getAll() {
        return repository.findAll();
    }
}