package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessWithdrawUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
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

        log.info("Received CREDIT_CARD_PAYMENT_OCCURRED. txId={}",event.getTransactionId());

//        Transaction tx=new Transaction(
//                event.getTransactionId().toString(),
//                event.getCustomerId().toString(),
//                null,
//                null,
//                event.getAccountId().toString(),
//                null,
//                TransactionType.WITHDRAW,
//                new BigDecimal(event.getAmount()),
//                BigDecimal.ZERO,
//                "TO CREDIT PAYMENT",
//                null
//        );
        AccountWithdrawCommand cmd=new AccountWithdrawCommand(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                "TO CREDIT CARD PAYMENT",
                event.getAccountId().toString(),
                new BigDecimal(event.getAmount())
        );

        withDrawUseCase.execute(cmd).subscribe();
    }
}
