package pe.nom.charlygastelo.app.accountservice.domain.port;

import io.reactivex.rxjava3.core.Completable;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;

public interface TransactionEventPort {

    Completable publishTransactionCompleted(Transaction transaction);

    Completable publishTransactionFailed(Transaction transaction, String reason);
}