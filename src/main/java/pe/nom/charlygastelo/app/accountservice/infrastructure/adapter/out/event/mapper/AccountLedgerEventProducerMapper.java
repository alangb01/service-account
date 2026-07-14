package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDepositOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountInitialDepositEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountWithdrawOccurredEvent;

import java.time.Instant;
import java.util.UUID;

@Component
public class AccountLedgerEventProducerMapper {

//    public AccountDepositOccurredEvent toAccountDepositOccurredEvent(Account account) {
//        return AccountDepositOccurredEvent.newBuilder()
//                .setEventId(UUID.randomUUID().toString())
//                .setEventType("ACCOUNT_DEPOSIT_OCCURRED")
//                .setOccurredAt(Instant.now().toString())
//                .setVersion("1.0")
//                .setSource("account-service")
//                .setAccountId(account.id())
//                .setCustomerId(account.customerId())
//                .setAmount(account.balance().toPlainString())
//                .setBalance(account.balance().toPlainString())
//                .setAvailable(account.balance().toPlainString())
//                .build();
//    }
//
//    public AccountWithdrawOccurredEvent toAccountWithdrawOccurredEvent(Transaction tx) {
//        return AccountWithdrawOccurredEvent.newBuilder()
//                .setEventId(UUID.randomUUID().toString())
//                .setEventType("ACCOUNT_WITHDRAW_OCCURRED")
//                .setOccurredAt(Instant.now().toString())
//                .setVersion("1.0")
//                .setSource("account-service")
//                .setTransactionId(tx.id())
//                .setCustomerId(tx.customerId())
//                .setAccountId(tx.sourceProductId())
//                .setAmount(tx.amount().doubleValue())
////                .setBalance(tx.balance().toPlainString())
////                .setAvailable(tx.available().toPlainString())
//                .build();
//    }
    public AccountDepositOccurredEvent toAccountDepositOccurredEvent(Transaction transaction, Account target) {
        return AccountDepositOccurredEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_DEPOSIT_OCCURRED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setTransactionId(transaction.id())
                .setAccountId(target.id())
                .setCustomerId(target.customerId())
                .setAmount(target.balance().toPlainString())
                .setBalance(target.balance().toPlainString())
                .setAvailable(target.available().toPlainString())
                .build();
}

    public AccountWithdrawOccurredEvent toAccountWithdrawOccurredEvent(Transaction tx, Account ac) {
        return AccountWithdrawOccurredEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_WITHDRAW_OCCURRED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setTransactionId(tx.id())
                .setCustomerId(tx.customerId())
                .setAccountId(tx.sourceProductId())
                .setAmount(tx.amount().doubleValue())
                .setBalance(ac.balance().toPlainString())
                .setAvailable(ac.available().toPlainString())
                .setReason(tx.type().name())
                .build();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    public AccountInitialDepositEvent toAccountInitialDeposit(Account account) {
        System.out.println("account = " + account);
        return AccountInitialDepositEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("INITIAL_DEPOSIT")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setAccountId(value(account.id()))
                .setCustomerId(value(account.customerId()))
                .setAmount(account.balance().doubleValue())
                .setAvailable(account.available().doubleValue())
                .setCurrency(account.currency())
                .setBalance(account.balance().doubleValue())
                .setAvailable(account.available().doubleValue())
                .build();

    }

}
