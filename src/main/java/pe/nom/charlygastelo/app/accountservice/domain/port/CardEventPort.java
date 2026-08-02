package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.domain.model.Card;

public interface CardEventPort {

    Single<Card> getById(String cardId);
}