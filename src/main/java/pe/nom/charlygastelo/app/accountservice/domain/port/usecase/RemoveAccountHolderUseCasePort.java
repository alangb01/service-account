package pe.nom.charlygastelo.app.accountservice.domain.port.usecase;

import io.reactivex.rxjava3.core.Completable;

public interface RemoveAccountHolderUseCasePort {
    Completable delete(String id);
}