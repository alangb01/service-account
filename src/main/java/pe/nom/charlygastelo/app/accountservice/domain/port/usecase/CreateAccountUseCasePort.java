package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

public interface CreateAccountUseCasePort {
    Single<Account> create(Account request, String token);
}