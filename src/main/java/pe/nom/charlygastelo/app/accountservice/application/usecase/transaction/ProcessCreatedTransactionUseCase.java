package pe.nom.charlygastelo.app.accountservice.application.usecase.transaction;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.TransactionEventProducer;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessCreatedTransactionUseCase {
    private final ProcessDepositUseCase processDepositUseCase;
    private final ProcessWithdrawlUseCase processWithdrawUseCase;
    private final ProcessTransferUseCase processTransferUseCase;
    private final ProcessTransferToThirdUseCase processTransferToThirdUseCase;

    public Completable execute(Transaction transaction, String correlationId) {
        return switch (transaction.type()) {
            case "DEPOSIT" -> processDepositUseCase.execute(transaction);
            case "WITHDRAWAL" -> processWithdrawUseCase.execute(transaction);
            case "TRANSFER" -> processTransferUseCase.execute(transaction);
            case "TRANSFER_TO_THIRD" -> processTransferToThirdUseCase.execute(transaction);
            case "CREDIT_PAYMENT" -> processWithdrawUseCase.execute(transaction);
            default -> {
                log.warn("[TX-SERVICE] Transaction ignored. type={}, txId={}, correlationId={}, timestamp={}",
                        transaction.type(), transaction.id(), correlationId, Instant.now());
                yield Completable.complete();
            }
        };
    }
}
