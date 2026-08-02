package pe.nom.charlygastelo.app.accountservice.domain.port.event;

import io.reactivex.rxjava3.core.Completable;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

public interface AccountManagementEventProducerPort {
    Completable publishAccountCreated(Account account);

    Completable publishAccountUpdated(Account account);

    Completable publishAccountClosed(Account account);

    Completable publishAccountDeleted(String accountId);
}
