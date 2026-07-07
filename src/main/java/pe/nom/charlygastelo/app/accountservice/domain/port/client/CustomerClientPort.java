package pe.nom.charlygastelo.app.accountservice.domain.port.client;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import reactor.core.publisher.Mono;

public interface CustomerClientPort {
    Single<Customer> getById(String id, String token);

}
