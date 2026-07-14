package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.mapper.AccountPersistentMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountDocument;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.repository.AccountReactiveRepository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AccountRepository implements AccountRepositoryPort {

    private final AccountReactiveRepository repository;
    private final AccountPersistentMapper mapper;

    @Override
    public Single<Account> save(Account account) {
        log.debug("account = {}", account);
        AccountDocument document=mapper.toCreateDocument(account);
        log.debug("document = {}", document);
        return Single.fromPublisher(
                repository.save(document)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Maybe<Account> findById(String id) {
        return Maybe.fromPublisher(
                repository.findById(id)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Flowable<Account> findByCustomerId(String customerId) {
        return Flowable.fromPublisher(
                repository.findByCustomerId(customerId)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Maybe<Account> findByNumber(String number) {
        return Maybe.fromPublisher(
                repository.findByNumber(number)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Flowable<Account> findByCustomerIdAndType(
            String customerId,
            String type) {

        return Flowable.fromPublisher(
                repository.findByCustomerIdAndType(customerId, type)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Single<Boolean> existsById(String id) {
        return Single.fromPublisher(repository.existsById(id));
    }

    @Override
    public Flowable<Account> findAll() {
        return Flowable.fromPublisher(
                repository.findAll()
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Completable deleteById(String id) {
        return Completable.fromPublisher(repository.deleteById(id));
    }
}