package pe.nom.charlygastelo.app.accountservice.domain.port.repository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;
import pe.nom.charlygastelo.app.accountservice.domain.model.SignerRole;

import java.util.List;

public interface AccountSignerRepositoryPort {

    Single<AccountSigner> save(AccountSigner account);

    Maybe<AccountSigner> findById(String id);

    Completable delete(String id);

    Flowable<AccountSigner> findByAccountId(String accountId);

    Maybe<AccountSigner> findActiveSigner(String accountId, String customerId, SignerRole signerRole);
}
