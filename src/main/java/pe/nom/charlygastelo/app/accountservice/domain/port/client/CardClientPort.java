package pe.nom.charlygastelo.app.accountservice.domain.port.client;

import io.reactivex.rxjava3.core.Single;

public interface CardClientPort {
    Single<Boolean> hasActiveCreditCard(String id, String token);
}
