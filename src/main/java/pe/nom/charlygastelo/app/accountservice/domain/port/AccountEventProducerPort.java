package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Completable;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import reactor.core.publisher.Mono;

public interface AccountEventProducerPort {
    Completable publishAccountCreated(Account account);
    Completable publishAccountUpdated(Account account);
    Completable publishAccountClosed(Account account);
    Completable publishAccountDeleted(String accountid);
}
