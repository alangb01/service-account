package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.*;

import java.time.Instant;
import java.util.UUID;

@Component
public class AccountLedgerEventProducerMapper {
    private static final String VERSION = "1.0";
    private static final String SOURCE = "account-service";

    private String now() {
        return Instant.now().toString();
    }

    private String id() {
        return UUID.randomUUID().toString();
    }

    public AccountDepositOccurredEvent toAccountDepositOccurredEvent(Transaction tx, Account target) {
        return AccountDepositOccurredEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_DEPOSIT_OCCURRED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")
                .setTransactionId(tx.id())
                .setAccountId(target.id())
                .setCustomerId(target.customerId())
                .setAmount(tx.amount().toPlainString())
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
                .setBalance(ac.balance().doubleValue())
                .setAvailable(ac.available().doubleValue())
                .setReason(tx.type().name())
                .build();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }


    // ------------------------------------------------------------
    // TRANSFER BETWEEN ACCOUNTS
    // ------------------------------------------------------------
    public AccountTransferOccurredEvent toAccountTransferOccurredEvent(
            Transaction tx,
            Account source,
            Account target
    ) {
        return AccountTransferOccurredEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_TRANSFER_OCCURRED")
                .setOccurredAt(now())
                .setVersion(VERSION)
                .setSource(SOURCE)

                .setTransactionId(tx.id())
                .setCustomerId(tx.customerId())

                .setSourceAccountId(source.id())
                .setTargetAccountId(target.id())

                .setAmount(tx.amount().toPlainString())

                .setSourceBalance(source.balance().toPlainString())
                .setSourceAvailable(source.available().toPlainString())

                .setTargetBalance(target.balance().toPlainString())
                .setTargetAvailable(target.available().toPlainString())

                .build();
    }

    // ------------------------------------------------------------
    // TRANSFER TO THIRD PARTY
    // ------------------------------------------------------------
    public AccountTransferToThirdOccurredEvent toAccountTransferToThirdOccurredEvent(
            Transaction tx,
            Account source,
            Account third
    ) {
        return AccountTransferToThirdOccurredEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_TRANSFER_TO_THIRD_OCCURRED")
                .setOccurredAt(now())
                .setVersion(VERSION)
                .setSource(SOURCE)

                .setTransactionId(tx.id())
                .setCustomerId(tx.customerId())

                .setSourceAccountId(source.id())
                .setThirdPartyAccountId(third.id())

                .setAmount(tx.amount().toPlainString())

                .setSourceBalance(source.balance().toPlainString())
                .setSourceAvailable(source.available().toPlainString())

                .setThirdPartyBalance(third.balance().toPlainString())
                .setThirdPartyAvailable(third.available().toPlainString())

                .build();
    }

    // ------------------------------------------------------------
    // CREDIT PAYMENT
    // ------------------------------------------------------------
    public AccountCreditPaymentEvent toAccountCreditPaymentEvent(
            Account account,
            Transaction tx
    ) {
        return AccountCreditPaymentEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_CREDIT_PAYMENT_OCCURRED")
                .setOccurredAt(now())
                .setVersion(VERSION)
                .setSource(SOURCE)

                .setTransactionId(tx.id())
                .setCustomerId(tx.customerId())

                .setSourceAccountId(account.id())
                .setCreditId(tx.targetProductId())

                .setAmount(tx.amount().toPlainString())

                .setSourceBalance(account.balance().toPlainString())
                .setSourceAvailable(account.available().toPlainString())

                // Si el crédito tiene balance, lo agregas aquí
//                .setCreditBalance(tx.targetBalance().toPlainString())
//                .setCreditAvailable(tx.targetAvailable().toPlainString())

                .build();
    }

    // ------------------------------------------------------------
    // DEBIT CARD PAYMENT
    // ------------------------------------------------------------
    public AccountDebitCardPaymentEvent toAccountDebitCardPaymentEvent(
            Account account,
            Transaction tx
    ) {
        return AccountDebitCardPaymentEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_DEBIT_CARD_PAYMENT_OCCURRED")
                .setOccurredAt(now())
                .setVersion(VERSION)
                .setSource(SOURCE)

                .setTransactionId(tx.id())
                .setCustomerId(tx.customerId())

                .setSourceAccountId(account.id())
                .setCardId(tx.targetProductId())

                .setAmount(tx.amount().toPlainString())

                .setSourceBalance(account.balance().toPlainString())
                .setSourceAvailable(account.available().toPlainString())

                .build();
    }

    // ------------------------------------------------------------
    // YANKI PAYMENT
    // ------------------------------------------------------------
    public AccountYankiPaymentEvent toAccountYankiPaymentEvent(
            Account account,
            Transaction tx
    ) {
        return AccountYankiPaymentEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_YANKI_PAYMENT_OCCURRED")
                .setOccurredAt(now())
                .setVersion(VERSION)
                .setSource(SOURCE)

                .setTransactionId(tx.id())
                .setCustomerId(tx.customerId())

                .setSourceAccountId(account.id())
                .setYankiWalletId(tx.targetProductId())

                .setAmount(tx.amount().toPlainString())

                .setSourceBalance(account.balance().toPlainString())
                .setSourceAvailable(account.available().toPlainString())

                .build();
    }
}
