package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Single;

public interface CustomerEventProducerPort {
    Single<Void> getById(String customerId);
}
