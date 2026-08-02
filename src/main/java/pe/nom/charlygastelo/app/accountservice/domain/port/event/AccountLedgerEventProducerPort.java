package pe.nom.charlygastelo.app.accountservice.domain.port.event;

import io.reactivex.rxjava3.core.Completable;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDepositOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountInitialDepositEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountWithdrawOccurredEvent;

import java.math.BigDecimal;

public interface AccountLedgerEventProducerPort {

    Completable publishAccountWithdrawOccurred(ProcessedTransaction procesed);

    Completable publishAccountDepositOccurred(ProcessedTransaction procesed);

    Completable publishAccountTransferOccurred(ProcessedTransaction procesed);
    Completable publishAccountTransferToThirdOccurred(ProcessedTransaction procesed);

    Completable publishCreditPaymentCompleted(ProcessedTransaction procesed);

    Completable publishDebitCardPaymentCompleted(ProcessedTransaction procesed);

    Completable publishYankiPaymentCompleted(ProcessedTransaction procesed);
}
