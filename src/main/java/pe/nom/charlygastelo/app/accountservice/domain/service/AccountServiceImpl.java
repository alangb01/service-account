package pe.nom.charlygastelo.app.accountservice.domain.service;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;

import java.util.List;
import java.util.Optional;

public class AccountServiceImpl implements AccountServicePort {

    private final AccountRepositoryPort repository;

    public AccountServiceImpl(AccountRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Account create(Account account) {
        // Aquí puedes aplicar reglas de negocio:
        // - validar número único
        // - validar moneda
        // - validar cliente existente
        return repository.save(account);
    }

    @Override
    public Optional<Account> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Account> getByNumber(String number) {
        return repository.findByNumber(number);
    }

    @Override
    public List<Account> getByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public List<Account> getAll() {
        return repository.findAll();
    }
}
