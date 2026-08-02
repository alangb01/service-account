package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class AccountLedgerEventProducerMapper {

    private String now() {
        return Instant.now().toString();
    }

    private String id() {
        return UUID.randomUUID().toString();
    }

    public AccountDepositOccurredEvent toAccountDepositOccurredEvent(ProcessedTransaction processedTransaction) {
        return AccountDepositOccurredEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_DEPOSIT_OCCURRED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.customerId())
                .setAccountId(processedTransaction.target().id())
                .setAmount(processedTransaction.amount().doubleValue())
                .setBalance(processedTransaction.target().balance().doubleValue())
                .setAvailable(processedTransaction.target().available().doubleValue())

                .build();
}

    public AccountWithdrawOccurredEvent toAccountWithdrawOccurredEvent(ProcessedTransaction processedTransaction) {

        return AccountWithdrawOccurredEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ACCOUNT_WITHDRAW_OCCURRED")
                .setOccurredAt(Instant.now().toString())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.customerId())
                .setAccountId(processedTransaction.source().id())
                .setAmount(processedTransaction.amount().doubleValue())
                .setBalance(processedTransaction.source().balance().doubleValue())
                .setAvailable(processedTransaction.source().available().doubleValue())

                .build();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }


    // ------------------------------------------------------------
    // TRANSFER BETWEEN ACCOUNTS
    // ------------------------------------------------------------
    public AccountTransferOccurredEvent toAccountTransferOccurredEvent(ProcessedTransaction processedTransaction) {
        return AccountTransferOccurredEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_TRANSFER_OCCURRED")
                .setOccurredAt(now())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.source().customerId())

                .setSourceAccountId(processedTransaction.source().id())
                .setTargetAccountId(processedTransaction.target().id())

                .setAmount(processedTransaction.amount().doubleValue())

                .setSourceBalance(processedTransaction.source().balance().doubleValue())
                .setSourceAvailable(processedTransaction.source().available().doubleValue())

                .setTargetBalance(processedTransaction.target().balance().doubleValue())
                .setTargetAvailable(processedTransaction.target().available().doubleValue())

                .build();
    }

    // ------------------------------------------------------------
    // TRANSFER TO THIRD PARTY
    // ------------------------------------------------------------
    public AccountTransferToThirdOccurredEvent toAccountTransferToThirdOccurredEvent(
            ProcessedTransaction processedTransaction
    ) {
        return AccountTransferToThirdOccurredEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_TRANSFER_TO_THIRD_OCCURRED")
                .setOccurredAt(now())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.customerId())

                .setSourceAccountId(processedTransaction.source().id())
                .setThirdPartyAccountId(processedTransaction.target().id())

                .setAmount(processedTransaction.amount().toString())

                .setSourceBalance(processedTransaction.source().balance().toString())
                .setSourceAvailable(processedTransaction.source().available().toString())

                .setThirdPartyBalance(processedTransaction.target().balance().toString())
                .setThirdPartyAvailable(processedTransaction.target().available().toString())

                .build();
    }

    // ------------------------------------------------------------
    // CREDIT PAYMENT
    // ------------------------------------------------------------
    public AccountCreditPaymentEvent toAccountCreditPaymentEvent(
            ProcessedTransaction processedTransaction
    ) {
        return AccountCreditPaymentEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_CREDIT_PAYMENT_OCCURRED")
                .setOccurredAt(now())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.customerId())

                .setSourceAccountId(processedTransaction.source().id())

                .setAmount(processedTransaction.amount().toString())

                .setSourceBalance(processedTransaction.source().balance().toString())
                .setSourceAvailable(processedTransaction.source().available().toString())

                // Si el crédito tiene balance, lo agregas aquí
//                .setCreditBalance(tx.targetBalance().toPlainString())
//                .setCreditAvailable(tx.targetAvailable().toPlainString())

                .build();
    }

    // ------------------------------------------------------------
    // DEBIT CARD PAYMENT
    // ------------------------------------------------------------
    public AccountDebitCardPaymentEvent toAccountDebitCardPaymentEvent(
            ProcessedTransaction processedTransaction
    ) {
        return AccountDebitCardPaymentEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_DEBIT_CARD_PAYMENT_OCCURRED")
                .setOccurredAt(now())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.customerId())

                .setSourceAccountId(processedTransaction.source().id())

                .setAmount(processedTransaction.amount().toString())

                .setSourceBalance(processedTransaction.source().balance().toString())
                .setSourceAvailable(processedTransaction.source().available().toString())

                .build();
    }

    // ------------------------------------------------------------
    // YANKI PAYMENT
    // ------------------------------------------------------------
    public AccountYankiPaymentEvent toAccountYankiPaymentEvent(
            ProcessedTransaction processedTransaction
    ) {
        return AccountYankiPaymentEvent.newBuilder()
                .setEventId(id())
                .setEventType("ACCOUNT_YANKI_PAYMENT_OCCURRED")
                .setOccurredAt(now())
                .setVersion("1.0")
                .setSource("account-service")

                .setTransactionId(processedTransaction.transactionId())
                .setCustomerId(processedTransaction.customerId())

                .setSourceAccountId(processedTransaction.source().id())

                .setAmount(processedTransaction.amount().doubleValue())

                .setSourceBalance(processedTransaction.source().balance().doubleValue())
                .setSourceAvailable(processedTransaction.source().available().doubleValue())

                .build();
    }
}
