package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.exception.CustomerServiceUnavailableException;
import pe.nom.charlygastelo.app.shared.avro.dto.CustomerResponseEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CustomerResponseRegistry {

    private final Map<String, SingleEmitter<Customer>> pendingRequests =
            new ConcurrentHashMap<>();

    public Single<Customer> waitForResponse(String correlationId) {
        return Single.<Customer>create(emitter ->
                pendingRequests.put(correlationId, emitter)
        ).timeout(2, TimeUnit.SECONDS)
                .doFinally(() -> pendingRequests.remove(correlationId));
    }

    public void complete(CustomerResponseEvent event) {
        String correlationId = event.getCorrelationId().toString();

        SingleEmitter<Customer> emitter =
                pendingRequests.remove(correlationId);

        if (emitter == null) {
            return;
        }

        if (!event.getFound()) {
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

        emitter.onSuccess(customer);
    }
}