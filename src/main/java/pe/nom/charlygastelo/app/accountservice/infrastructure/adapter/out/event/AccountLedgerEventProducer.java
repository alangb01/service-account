package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AccountLedgerEventProducerMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.*;

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
    public Completable publishAccountWithdrawOccurred(Account ac, Transaction tx) {
        AccountWithdrawOccurredEvent event = mapper.toAccountWithdrawOccurredEvent(tx,ac);

        return  publish(
                accountWithdrawOccurred,
                event.getAccountId().toString(),
                event
        );
    }

    @Override
    public Completable publishAccountDepositOccurred(Account ac, Transaction tx) {
        AccountDepositOccurredEvent event = mapper.toAccountDepositOccurredEvent(tx,ac);
        return  publish(
                accountDepositOccurred,
                event.getAccountId().toString(),
                event
        );
    }

    @Override
    public Completable publishAccountTransferOccurred(Account source, Account target, Transaction tx) {
        AccountTransferOccurredEvent event =
                mapper.toAccountTransferOccurredEvent(tx, source, target);

        return publish(
                accountTransferOccurredTopic,          
                event.getSourceAccountId().toString(), 
                event                                 
        );
    }

    @Override
    public Completable publishAccountTransferToThirdOccurred(Account source, Account third, Transaction tx) {
        AccountTransferToThirdOccurredEvent event =
                mapper.toAccountTransferToThirdOccurredEvent(tx, source, third);

        return publish(
                accountTransferToThirdOccurredTopic,
                event.getSourceAccountId().toString(),
                event                                  
        );
    }

    @Override
    public Completable publishCreditPaymentCompleted(Account account, Transaction transaction) {
        AccountCreditPaymentEvent event=mapper.toAccountCreditPaymentEvent(account, transaction);
        return publish(
                accountCreditPaymentTopic,   
                event.getSourceAccountId().toString(), 
                event                                 
        );
    }

    @Override
    public Completable publishDebitCardPaymentCompleted(Account account, Transaction transaction) {
        AccountDebitCardPaymentEvent event=mapper.toAccountDebitCardPaymentEvent(account, transaction);
        return publish(
                accountDebitCardPaymentTopic,   
                event.getSourceAccountId().toString(), 
                event                                 
        );
    }

    @Override
    public Completable publishYankiPaymentCompleted(Account account, Transaction transaction) {
        AccountYankiPaymentEvent event=mapper.toAccountYankiPaymentEvent(account, transaction);
        return publish(
                accountYankiPaymentTopic,   
                event.getSourceAccountId().toString(), 
                event                                 
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
