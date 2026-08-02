package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Completable;

public interface RemoveAccountSignerUseCasePort {
    Completable delete(String id);
}