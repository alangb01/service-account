package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerResponseEvent;

@Slf4j
@Component
public class CustomerResponseRegistry {

    @Value("${customer.response.timeout:2}")
    private int timeout;

    private final Map<String, SingleEmitter<Customer>> pendingRequests =
            new ConcurrentHashMap<>();

    public Single<Customer> waitForResponse(String correlationId) {
        log.info("Waiting CustomerResponseEvent. correlationId={}, timeout={}s",
                correlationId, timeout);

        return Single.<Customer>create(emitter ->
                        pendingRequests.put(correlationId, emitter)
                )
                .timeout(timeout, TimeUnit.SECONDS)
                .doFinally(() -> {
                    pendingRequests.remove(correlationId);
                    log.debug("Customer pending request removed. correlationId={}",
                            correlationId);
                });
    }

    public void complete(CustomerResponseEvent event) {
        String correlationId = event.getCorrelationId().toString();

        log.info("Completing CustomerResponseEvent. correlationId={}, found={}",
                correlationId, event.getFound());

        SingleEmitter<Customer> emitter = pendingRequests.remove(correlationId);

        if (emitter == null) {
            log.warn("No pending customer request found. correlationId={}",
                    correlationId);
            return;
        }

        if (!event.getFound()) {
            log.warn("Customer not found from response. correlationId={}, customerId={}",
                    correlationId, event.getCustomerId());

            emitter.onError(new CustomerNotFoundException("Customer not found"));
            return;
        }

        Customer customer = new Customer(
                event.getCustomerId().toString(),
                event.getCustomerType().toString(),
                event.getDocumentType().toString(),
                event.getDocumentNumber().toString(),
                event.getName().toString(),
                event.getLastName().toString(),
                event.getEmail().toString(),
                event.getPhone().toString(),
                event.getActive()
        );

        log.info("Customer response completed successfully. correlationId={}, customerId={}",
                correlationId, customer.id());

        emitter.onSuccess(customer);
    }
}