package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountDepositCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountTransferCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.validate.ValidateTransferUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.ProcessedTransaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.AccountLedgerEventProducer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

@RequiredArgsConstructor
@Component
@Slf4j
public class ProcessTransferUseCase {
    private final ProcessDepositUseCase processDepositUseCase;
    private final ProcessWithdrawUseCase processWithdrawUseCase;

    public Completable execute(AccountTransferCommand cmd) {
        log.info("Processing transfer transaction: {}", cmd.transactionId());

        AccountWithdrawCommand withdrawCmd=new AccountWithdrawCommand(
                cmd.transactionId(),
                cmd.customerId(),
                cmd.descripcion(),
                cmd.sourceAccountId(),
                cmd.amount()
        );

        Completable processWithdraw=processWithdrawUseCase.execute(withdrawCmd);

        AccountDepositCommand depositCmd=new AccountDepositCommand(
                cmd.transactionId(),
                cmd.customerId(),
                cmd.descripcion(),
                cmd.targetAccountId(),
                cmd.amount()
        );

        Completable processDeposit=processDepositUseCase.execute(depositCmd);



        return processWithdraw.andThen(processDeposit);

    }
}
