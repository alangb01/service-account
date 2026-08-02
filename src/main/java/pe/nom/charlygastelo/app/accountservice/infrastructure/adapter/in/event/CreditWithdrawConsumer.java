package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.ProcessDepositUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountDepositCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;
import pe.nom.charlygastelo.app.shared.avro.dto.CreditWithdrawOccurredEvent;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditWithdrawConsumer {

    private final ProcessDepositUseCase depositUseCase;

    @KafkaListener(
            topics = "${topic.credit-withdraw-occurred}",
            groupId = "account-service"
    )
    public void consume(CreditWithdrawOccurredEvent event) {
        log.info("[ACCOUNT] Received CREDIT_WITHDRAW_OCCURRED. txId={}, creditId={}, accountId={}, amount={}",
                event.getTransactionId(), event.getCreditId(), event.getAccountId(), event.getAmount());

//        Transaction tx=new Transaction(
//                event.getTransactionId().toString(),
//                event.getCustomerId().toString(),
//                null,
//                null,
//                event.getCreditId().toString(),
//                event.getAccountId().toString(),
//                TransactionType.DEPOSIT,
//                new BigDecimal(event.getAmount()),
//                BigDecimal.ZERO,
//                "FROM CREDIT",
//                null
//        );

        AccountDepositCommand cmd=new AccountDepositCommand(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                "TO CREDIT WITHDRAW",
                event.getAccountId().toString(),
                new BigDecimal(event.getAmount())
        );


        depositUseCase.execute(cmd).subscribe();
    }
}
