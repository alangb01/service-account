package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import reactor.core.publisher.Mono;

public interface AccountEventProducerPort {
    Completable publishAccountCreated(Account account);
    Completable publishAccountUpdated(Account account);
    Completable publishAccountClosed(Account account);
    Completable publishAccountDeleted(String accountid);

    Completable publishAccountInitialDeposit(Account saved);
}
