package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AccountLedgerEventProducerMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.*;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLedgerEventProducer implements AccountLedgerEventProducerPort {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final AccountLedgerEventProducerMapper mapper;

    @Value("${topic.account-withdraw-occurred}")
    private String accountWithdrawOccurred;

    @Value("${topic.account-deposit-occurred}")
    private String accountDepositOccurred;

    @Value("${topic.account-transfer-occurred}")
    private String accountTransferOccurredTopic;

    @Value("${topic.account-transfer-to-third-occurred}")
    private String accountTransferToThirdOccurredTopic;

    @Value("${topic.account-credit-payment-occurred}")
    private String accountCreditPaymentTopic;

    @Value("${topic.account-debit-card-payment-occurred}")
    private String accountDebitCardPaymentTopic;

    @Value("${topic.account-yanki-payment-occurred}")
    private String accountYankiPaymentTopic;

    @Override
    public Completable publishAccountWithdrawOccurred(ProcessedTransaction processedTransaction) {

        return  publish(
                accountWithdrawOccurred,
                processedTransaction.transactionId(),
                mapper.toAccountWithdrawOccurredEvent(processedTransaction)
        );
    }

    @Override
    public Completable publishAccountDepositOccurred(ProcessedTransaction processedTransaction) {
        return  publish(
                accountDepositOccurred,
                processedTransaction.transactionId(),
                mapper.toAccountDepositOccurredEvent(processedTransaction)
        );
    }

    @Override
    public Completable publishAccountTransferOccurred(ProcessedTransaction processedTransaction) {
        return publish(
                accountTransferOccurredTopic,          
                processedTransaction.transactionId(),
                mapper.toAccountTransferOccurredEvent(processedTransaction)
        );
    }

    @Override
    public Completable publishAccountTransferToThirdOccurred(ProcessedTransaction processedTransaction) {


        return publish(
                accountTransferToThirdOccurredTopic,
                processedTransaction.transactionId(),
                mapper.toAccountTransferToThirdOccurredEvent(processedTransaction)                                  
        );
    }

    @Override
    public Completable publishCreditPaymentCompleted(ProcessedTransaction processedTransaction) {

        return publish(
                accountCreditPaymentTopic,   
                processedTransaction.transactionId(),
                mapper.toAccountCreditPaymentEvent(processedTransaction)
        );
    }

    @Override
    public Completable publishDebitCardPaymentCompleted(ProcessedTransaction processedTransaction) {
        AccountDebitCardPaymentEvent event=mapper.toAccountDebitCardPaymentEvent(processedTransaction);
        return publish(
                accountDebitCardPaymentTopic,   
                processedTransaction.transactionId(), 
                event                                 
        );
    }

    @Override
    public Completable publishYankiPaymentCompleted(ProcessedTransaction processedTransaction) {
        return publish(
                accountYankiPaymentTopic,   
                processedTransaction.transactionId(),
                mapper.toAccountYankiPaymentEvent(processedTransaction)
        );
    }

    private Completable publish(String topic, String key, SpecificRecordBase event) {
        log.info("[ACCOUNT-EVENT] Sending event. topic={}, key={}", topic, key);
        return Completable.fromFuture(
            kafkaTemplate.send(topic, key, event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[ACCOUNT-EVENT] Error sending event. topic={}, key={}, reason={}",
                                topic, key, error.getMessage(), error);
                    } else {
                        log.info("[ACCOUNT-EVENT] Event sent successfully. topic={}, key={}, partition={}, offset={}",
                            topic, key,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                })
            );
    }

}
