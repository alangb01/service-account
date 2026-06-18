package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

import java.util.List;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;

    public AccountRepositoryAdapter(SpringDataAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountEntity.fromDomain(account);
        return repository.save(entity).toDomain();
    }

    @Override
    public Optional<Account> findById(String id) {
        return repository.findById(id).map(AccountEntity::toDomain);
    }

    @Override
    public Optional<Account> findByNumber(String number) {
        return repository.findByNumber(number).map(AccountEntity::toDomain);
    }

    @Override
    public List<Account> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(AccountEntity::toDomain)
                .toList();
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll()
                .stream()
                .map(AccountEntity::toDomain)
                .toList();
    }
}