package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessWithdrawUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.TransactionEventConsumerMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.CreditCardPaymentOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.CreditPaymentOccurredEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditCardPaymentConsumer {
    private final ProcessWithdrawUseCase withDrawUseCase;
    private final TransactionEventConsumerMapper transactionMapper;

    @KafkaListener(
            topics = "${topic.credit-card-payment-occurred}",
            groupId = "account-service"
    )
    public void consume(CreditCardPaymentOccurredEvent event) {
        log.info("[ACCOUNT] Received CREDIT_CARD_PAYMENT_OCCURRED. txId={}, cardId={}, accountId={}, customerId={} amount={}",
                event.getTransactionId(), event.getCardId(), event.getAccountId(), event.getCustomerId(), event.getAmount());

        Transaction tx=new Transaction(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                null,
                null,
                event.getAccountId().toString(),
                null,
                TransactionType.WITHDRAW,
                new BigDecimal(event.getAmount()),
                BigDecimal.ZERO,
                "TO CREDIT PAYMENT",
                null
        );

        withDrawUseCase.execute(tx).subscribe();
    }
}
