package pe.nom.charlygastelo.app.accountservice.domain.service;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
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
        // Aquí puedes meter reglas de negocio (validar número único, etc.)
        return repository.save(account);
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