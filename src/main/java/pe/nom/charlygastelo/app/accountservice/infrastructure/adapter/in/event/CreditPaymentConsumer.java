package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessDepositUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessWithdrawUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper.TransactionEventConsumerMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.CreditPaymentOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.CreditWithdrawOccurredEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditPaymentConsumer {
    private final ProcessWithdrawUseCase withDrawUseCase;
    private final TransactionEventConsumerMapper transactionMapper;

    @KafkaListener(
            topics = "${topic.credit-payment-occurred}",
            groupId = "account-service"
    )
    public void consume(CreditPaymentOccurredEvent event) {
        log.info("[ACCOUNT] Received CREDIT_PAYMENT_OCCURRED. txId={}, creditId={}, accountId={}, customerId={} amount={}",
                event.getTransactionId(), event.getCreditId(), event.getAccountId(), event.getCustomerId(), event.getAmount());

//        Transaction tx=new Transaction(
//                event.getTransactionId().toString(),
//                event.getCustomerId().toString(),
//                null,
//                null,
//                event.getAccountId().toString(),
//                event.getCreditId().toString(),
//                TransactionType.WITHDRAW,
//                new BigDecimal(event.getAmount()),
//                BigDecimal.ZERO,
//                "TO CREDIT PAYMENT",
//                null
//        );
        AccountWithdrawCommand cmd=new AccountWithdrawCommand(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                "TO CREDIT PAYMENT",
                event.getAccountId().toString(),
                new BigDecimal(event.getAmount())
        );


        withDrawUseCase.execute(cmd).subscribe();
    }
}
