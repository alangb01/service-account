package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

import java.util.List;

public interface FindAccountUseCasePort {

    Maybe<Account> findById(String id);

    Single<List<Account>> findAll();

    Single<List<Account>> findByCustomerId(String customerId);

}