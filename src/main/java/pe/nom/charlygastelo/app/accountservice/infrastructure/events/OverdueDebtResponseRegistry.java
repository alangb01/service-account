package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.shared.avro.dto.OverdueDebtResponseEvent;

@Slf4j
@Component
public class OverdueDebtResponseRegistry {

    @Value("${overdue-debt.response.timeout:30}")
    private int timeout;

    private final Map<String, SingleEmitter<Boolean>> pendingRequests =
            new ConcurrentHashMap<>();

    public Single<Boolean> waitForResponse(String correlationId) {
        log.info("Waiting OverdueDebtResponseEvent. correlationId={}, timeout={}s",
                correlationId, timeout);

        return Single.<Boolean>create(emitter ->
                        pendingRequests.put(correlationId, emitter)
                )
                .timeout(timeout, TimeUnit.SECONDS)
                .doFinally(() -> {
                    pendingRequests.remove(correlationId);
                    log.debug("Overdue debt pending request removed. correlationId={}",
                            correlationId);
                });
    }

    public void complete(OverdueDebtResponseEvent event) {
        String correlationId = event.getCorrelationId().toString();

        SingleEmitter<Boolean> emitter = pendingRequests.remove(correlationId);

        if (emitter == null) {
            log.warn("No pending overdue debt request found. correlationId={}",
                    correlationId);
            return;
        }

        log.info("Overdue debt response completed. correlationId={}, customerId={}, hasDebt={}",
                correlationId,
                event.getCustomerId(),
                event.getHasOverdueDebt());

        emitter.onSuccess(event.getHasOverdueDebt());
    }
}