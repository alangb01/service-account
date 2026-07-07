package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

public interface UpdateAccountUseCasePort {
    Single<Account> update(String id, Account request);
}