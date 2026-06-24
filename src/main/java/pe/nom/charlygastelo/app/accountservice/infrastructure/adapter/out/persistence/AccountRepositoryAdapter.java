package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper.AccountPersistentMapper;

import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final ReactiveAccountRepository repository;
    private final AccountPersistentMapper mapper;

    @Override
    public Single<Account> save(Account account) {
        return Single.fromPublisher(repository.save(mapper.toDocument(account)).map(mapper::toDomain));
    }

    @Override
    public Maybe<Account> findById(String id) {
        return Maybe.fromPublisher(repository.findById(id).map(mapper::toDomain));
    }

    @Override
    public Flowable<Account> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(repository.findByCustomerId(customerId).map(mapper::toDomain));
    }

    @Override
    public Maybe<Account> findByNumber(String number) {
        return Maybe.fromPublisher(repository.findByNumber(number).map(mapper::toDomain));
    }

    @Override
    public Flowable<Account> findByCustomerIdAndType(String customerId, String type) {
        return Flowable.fromPublisher(repository.findByCustomerIdAndType(customerId, type).map(mapper::toDomain));
    }

    @Override
    public Single<Boolean> existsById(String id) {
        return Single.fromPublisher(
                repository.existsById(id)
        );
    }


    @Override
    public Flowable<Account> findAll() {
        return Flowable.fromPublisher(repository.findAll().map(mapper::toDomain));
    }


    @Override
    public Completable deleteById(String id) {
        return Completable.fromPublisher(repository.deleteById(id));
    }


}