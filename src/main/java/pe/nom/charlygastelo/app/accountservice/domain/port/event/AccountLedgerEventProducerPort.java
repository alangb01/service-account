package pe.nom.charlygastelo.app.accountservice.domain.port.event;

import io.reactivex.rxjava3.core.Completable;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDepositOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountInitialDepositEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountWithdrawOccurredEvent;

public interface AccountLedgerEventProducerPort {

    Completable publishAccountWithdrawOccurred(Account account, Transaction transaction);

    Completable publishAccountDepositOccurred(Account account, Transaction transaction);

    Completable publishAccountTransferOccurred(Account source, Account target, Transaction transaction);
    Completable publishAccountTransferToThirdOccurred(Account source, Account target, Transaction transaction);

    Completable publishCreditPaymentCompleted(Account account, Transaction transaction);

    Completable publishDebitCardPaymentCompleted(Account account, Transaction transaction);

    Completable publishYankiPaymentCompleted(Account account, Transaction transaction);
}
