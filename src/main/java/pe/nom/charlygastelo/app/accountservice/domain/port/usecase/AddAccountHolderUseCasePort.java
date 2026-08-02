package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;

public interface AddAccountHolderUseCasePort {
    Single<AccountHolder> add(AccountHolder request, String token);
}