package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;

public interface CustomerEventPort {
    Single<Customer> getById(String customerId);
}