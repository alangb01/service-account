package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountDepositCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountTransferCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.AccountWithdrawCommand;
import pe.nom.charlygastelo.app.accountservice.application.usecase.transaction.command.TransactionCommand;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.domain.model.TransactionType;



@Component
@RequiredArgsConstructor
@Slf4j
public class CreatedTransactionUseCase {
    private final ProcessTransferUseCase transferUseCase;
    private final ProcessDepositUseCase depositUseCase;
    private final ProcessWithdrawUseCase withdrawUseCase;

    public boolean isAccountServiceResponsible(TransactionType type) {
        return switch (type) {
            case
                 TRANSFER,
                 TRANSFER_TO_THIRD,
                 DEPOSIT,
                 WITHDRAW,
                 YANKI_SEND
                    -> true;

            default -> false;
        };
    }

    /**
     * Ejecuta el caso de uso correcto según el tipo de transacción.
     */
    public Completable execute(TransactionCommand cmd) {

        log.info("[ACCOUNT] Routing txId={} type={} to correct use case", cmd.transactionId(), cmd.transactionType());

        return switch (cmd.transactionType()) {

            case TRANSFER, TRANSFER_TO_THIRD, YANKI_SEND -> transferUseCase.execute(toTransferCommand(cmd));

            case DEPOSIT -> depositUseCase.execute(toDepositCommand(cmd));

            case WITHDRAW -> withdrawUseCase.execute(toWithdrawCommand(cmd));

            default -> {
                log.warn("[ACCOUNT] txId={} type={} ignored (not account responsibility)", cmd.transactionId(), cmd.transactionType());
                yield Completable.complete();
            }
        };
    }

    private AccountWithdrawCommand toWithdrawCommand(TransactionCommand cmd) {
        return new AccountWithdrawCommand(
                cmd.transactionId(),
                cmd.customerId(),
                cmd.descripcion(),
                cmd.sourceAccountId(),
                cmd.amount()
        );
    }

    private AccountDepositCommand toDepositCommand(TransactionCommand cmd) {
        return new AccountDepositCommand(
                cmd.transactionId(),
                cmd.customerId(),
                cmd.descripcion(),
                cmd.targetAccountId(),
                cmd.amount()
        );
    }

    private AccountTransferCommand toTransferCommand(TransactionCommand cmd) {
        return new AccountTransferCommand(
                cmd.transactionId(),
                cmd.customerId(),
                cmd.descripcion(),
                cmd.sourceAccountId(),
                cmd.targetAccountId(),
                cmd.amount()
        );
    }
}
