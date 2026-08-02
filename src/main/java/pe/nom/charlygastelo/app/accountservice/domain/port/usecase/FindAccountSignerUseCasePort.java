package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;

import java.util.List;

public interface FindAccountSignerUseCasePort {

    Maybe<AccountSigner> findById(String id);

    Single<List<AccountSigner>> findByAccountId(String accountId);

}