package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final ReactiveAccountRepository repository;

    public AccountRepositoryAdapter(ReactiveAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Account> save(Account account) {
        AccountDocument doc = toDocument(account);
        return repository.save(doc).map(this::toDomain);
    }

    @Override
    public Mono<Account> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<Account> findByNumber(String number) {
        return repository.findByNumber(number).map(this::toDomain);
    }

    @Override
    public Flux<Account> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId).map(this::toDomain);
    }

    @Override
    public Flux<Account> findAll() {
        return repository.findAll().map(this::toDomain);
    }


    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }

    private AccountDocument toDocument(Account a) {
        AccountDocument d = new AccountDocument();
        d.setId(a.id());
        d.setCustomerId(a.customerId());
        d.setCustomerType(a.customerType());
        d.setNumber(a.number());
        d.setType(a.type());
        d.setBalance(a.balance());
        d.setCurrency(a.currency());
        d.setCreatedAt(a.createdAt());
        d.setActive(a.active());
        d.setStatus(a.status());
        return d;
    }

    private Account toDomain(AccountDocument d) {
        return new Account(
                d.getId(),
                d.getCustomerId(),
                d.getCustomerType(),
                d.getNumber(),
                d.getType(),
                d.getBalance(),
                d.getCurrency(),
                d.getCreatedAt(),
                d.isActive(),
                d.getStatus()
        );
    }
}