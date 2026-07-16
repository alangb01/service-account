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
        AccountDocument document=mapper.toCreateDocument(account);
        log.debug("[ACCOUNT] Saving account. id={}, customerId={}", account.id(), account.customerId());
        return Single.fromPublisher(
                repository.save(document)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Maybe<Account> findById(String id) {
        log.debug("[ACCOUNT] Finding account by id. id={}", id);
        return Maybe.fromPublisher(
                repository.findById(id)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Flowable<Account> findByCustomerId(String customerId) {
        log.debug("[ACCOUNT] Saving account. customerId={}", customerId);
        return Flowable.fromPublisher(
                repository.findByCustomerId(customerId)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Maybe<Account> findByNumber(String number) {
        log.debug("[ACCOUNT] Finding account by number. number={}", number);
        return Maybe.fromPublisher(
                repository.findByNumber(number)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Flowable<Account> findByCustomerIdAndType(
            String customerId,
            String type) {
        log.debug("[ACCOUNT] Finding account by customerId and type. customerId={}, type={}", customerId, type);

        return Flowable.fromPublisher(
                repository.findByCustomerIdAndType(customerId, type)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Single<Boolean> existsById(String id) {
        log.debug("[ACCOUNT] Exists account by id. id={}", id);
        return Single.fromPublisher(repository.existsById(id));
    }

    @Override
    public Flowable<Account> findAll() {
        log.debug("[ACCOUNT] Finding all accounts.");
        return Flowable.fromPublisher(
                repository.findAll()
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Completable deleteById(String id) {
        log.debug("[ACCOUNT] Deleting account by id. id={}", id);
        return Completable.fromPublisher(repository.deleteById(id));
    }
}