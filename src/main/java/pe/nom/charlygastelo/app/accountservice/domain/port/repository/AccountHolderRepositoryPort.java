package pe.nom.charlygastelo.app.accountservice.domain.port.repository;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.support.InstanceSupplier;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;

import java.util.List;

public interface AccountHolderRepositoryPort {
    Single<AccountHolder> save(AccountHolder accountHolder);
    Completable delete(String id);
    Maybe<AccountHolder> findById(String id);
    Flowable<AccountHolder> findByAccountId(String accountId);

    Maybe<AccountHolder> findByAccountIdAndCustomerId(String accountId, String customerId);

    Maybe<AccountHolder> findOwner(String accountId, String customerId);

    Maybe<AccountHolder> findActiveHolder(String accountID, String customerId, HolderType holderType);

    Completable addOwner(String id, String s);
}
