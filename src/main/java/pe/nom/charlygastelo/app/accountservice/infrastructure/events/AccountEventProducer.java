package pe.nom.charlygastelo.app.accountservice.infrastructure.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountClosedEvent;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountCreatedEvent;
import pe.nom.charlygastelo.app.customerservice.infrastructure.avro.events.AccountUpdatedEvent;

import java.util.Map;

@Component
public class AccountEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AccountEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                     ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishAAccountCreated(AccountCreatedEvent event)  {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "accountId", event.getAccountId(),
                    "customerId", event.getCustomerId(),
                    "accountType", event.getAccountType(),
                    "initialBalance", event.getInitialBalance(),
                    "createdAt", event.getCreatedAt()
            ));
            kafkaTemplate.send("account.created", (String) event.getAccountId(), payload);
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    public void publishAAccountUpdated(AccountUpdatedEvent event)  {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "accountId", event.getAccountId(),
                    "customerId", event.getCustomerId(),
                    "accountType", event.getAccountType(),
                    "updatedAt", event.getUpdatedAt()
            ));
            kafkaTemplate.send("account.updated", (String) event.getAccountId(), payload);
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    public void publishAAccountDeleted(AccountClosedEvent event)  {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "accountId", event.getAccountId(),
                    "closedAt", event.getClosedAt()
            ));
            kafkaTemplate.send("account.closed", (String) event.getAccountId(), payload);
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

}
