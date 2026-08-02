package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import java.util.List;
import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

public interface ListAccountUseCasePort {

    Single<List<Account>> findAll();

    Single<List<Account>> findByCustomerId(String customerId);

}