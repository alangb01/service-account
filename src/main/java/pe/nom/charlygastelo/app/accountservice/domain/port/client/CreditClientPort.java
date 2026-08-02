package pe.nom.charlygastelo.app.accountservice.domain.port.client;

import io.reactivex.rxjava3.core.Single;

public interface CreditClientPort {
    Single<Boolean> hasOverdueDebt(String customerId, String token);
}
