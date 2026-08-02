package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;

public interface AddAccountSignerUseCasePort {
    Single<AccountSigner> add(AccountSigner request, String token);
}