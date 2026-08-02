package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Single;

public interface CreditEventPort {

    Single<Boolean> hasOverdueDebt(String customerId);

    Single<Boolean> hasActiveCreditCard(String customerId);
}