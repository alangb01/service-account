package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.adapter;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSignerStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.SignerRole;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountSignerRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.mapper.AccountSignerPersistentMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.repository.AccountSignerReactiveRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AccountSignerRepository implements AccountSignerRepositoryPort {

    private final AccountSignerReactiveRepository reactiveRepository;
    private final AccountSignerPersistentMapper mapper;

    @Override
    public Single<AccountSigner> save(AccountSigner account) {
        return Single.fromPublisher(
                reactiveRepository.save(mapper.toDocument(account))
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Maybe<AccountSigner> findById(String id) {
        return Maybe.fromPublisher(
                reactiveRepository.findById(id)
                        .map(mapper::toDomain)
        );
    }

    @Override
    public Completable delete(String id) {
        return Completable.fromPublisher(
                reactiveRepository.deleteById(id)
        );
    }

    @Override
    public Single<List<AccountSigner>> findByAccountId(String accountId){
        return Flowable.fromPublisher(
                    reactiveRepository.findByAccountId(accountId).map(mapper::toDomain)
            ).toList();
    }

    @Override
    public Maybe<AccountSigner> findActiveSigner(String accountId, String customerId, SignerRole signerRole) {
        return Maybe.fromPublisher(
                reactiveRepository.findByAccountIdAndCustomerIdAndSignerRoleAndStatus(
                        accountId,
                        customerId,
                        signerRole,
                        AccountSignerStatus.ACTIVE
                ).map(mapper::toDomain)
        );
    }


}