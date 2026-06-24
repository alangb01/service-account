package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

public interface AccountCachePort {

    Maybe<Account> getById(String id);

    Maybe<Account> getByNumber(String number);

    Completable save(Account account);

    Completable delete(String id);
}
