package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;

import java.util.List;

public interface FindAccountHolderUseCasePort {
    Single<List<AccountHolder>> findByAccountId(String accountId);

}