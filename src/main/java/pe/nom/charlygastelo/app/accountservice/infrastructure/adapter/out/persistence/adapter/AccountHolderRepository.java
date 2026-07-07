package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolderStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountHolderRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.mapper.AccountHolderPersistentMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.repository.AccountHolderReactiveRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AccountHolderRepository implements AccountHolderRepositoryPort {

    private final AccountHolderReactiveRepository reactiveRepository;
    private final AccountHolderPersistentMapper mapper;

    @Override
    public Single<AccountHolder> save(AccountHolder account) {
        return Single.fromPublisher(
               reactiveRepository.save(mapper.toDocument(account)).map(mapper::toDomain)
        );
    }

    @Override
    public Completable delete(String id) {
        return  Completable.fromPublisher(
                reactiveRepository.deleteById(id)
        ).doOnComplete(() ->
                log.info("AccountHolder {} deleted successfully", id)
        );
    }

    @Override
    public Maybe<AccountHolder> findById(String id) {
        return Maybe.fromPublisher(
                reactiveRepository.findById(id).map(mapper::toDomain)
        );
    }

    @Override
    public Single<List<AccountHolder>> findByAccountId(String accountId) {
        return Flowable.fromPublisher(
                reactiveRepository.findByAccountId(accountId).map(mapper::toDomain)
        ).toList();
    }

    @Override
    public Maybe<AccountHolder> findByAccountIdAndCustomerId(String accountId, String customerId) {
        return Maybe.fromPublisher(
                reactiveRepository.findByAccountIdAndCustomerId(accountId, customerId).map(mapper::toDomain)
        );
    }

    @Override
    public Maybe<AccountHolder> findOwner(String accountId, String customerId) {
        return Maybe.fromPublisher(
                reactiveRepository.findByAccountIdAndCustomerIdAndHolderType(accountId, customerId, HolderType.OWNER).map(mapper::toDomain)
        );
    }


    @Override
    public Maybe<AccountHolder> findActiveHolder(String accountId, String customerId, HolderType holderType) {
        return Maybe.fromPublisher(
                reactiveRepository.findByAccountIdAndCustomerIdAndStatus(accountId, customerId, AccountHolderStatus.ACTIVE).map(mapper::toDomain)
        );
    }

}