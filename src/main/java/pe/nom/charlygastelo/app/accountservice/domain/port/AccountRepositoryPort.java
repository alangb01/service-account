package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.checkerframework.checker.units.qual.A;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

public interface AccountRepositoryPort {

    Single<Account> save(Account account);

    Maybe<Account> findById(String id);

    Flowable<Account> findByCustomerId(String customerId);

    Flowable<Account> findAll();

    Completable deleteById(String id);

    Maybe<Account> findByNumber(String number);

    Flowable<Account> findByCustomerIdAndType(String customerId, String type);

    Single<Boolean> existsById(String id);
}
