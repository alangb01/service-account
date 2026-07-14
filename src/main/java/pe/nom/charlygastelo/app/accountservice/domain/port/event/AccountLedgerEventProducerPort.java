package pe.nom.charlygastelo.app.accountservice.domain.port.event;

import io.reactivex.rxjava3.core.Completable;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDepositOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountInitialDepositEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountWithdrawOccurredEvent;

public interface AccountLedgerEventProducerPort {
    Completable publishAccountWithdrawOccurred(AccountWithdrawOccurredEvent event);

    Completable publishAccountDepositOccurred(AccountDepositOccurredEvent event);

    Completable publishAccountInitialDepositOccurred(AccountInitialDepositEvent event);

    Completable publishAccountEventFor(Transaction transaction, Account source, Account target);
}
