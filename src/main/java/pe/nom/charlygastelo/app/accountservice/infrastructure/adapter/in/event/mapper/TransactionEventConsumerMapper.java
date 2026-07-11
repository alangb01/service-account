package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.event.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Transaction;
import pe.nom.charlygastelo.app.shared.avro.dto.TransactionCreatedEvent;

import java.math.BigDecimal;

@Component
public class TransactionEventConsumerMapper {
    public Transaction toDomain(TransactionCreatedEvent event){
        return new Transaction(
                event.getTransactionId().toString(),
                event.getCustomerId().toString(),
                value(event.getSourceProductType()),
                value(event.getTargetProductType()),
                value(event.getSourceProductId()),
                value(event.getTargetProductId()),
                value(event.getTransactionType()),
                BigDecimal.valueOf(event.getAmount()),
                BigDecimal.valueOf(event.getCommission()),
                event.getDescription().toString(),
                null
        );
    }

    private String value(CharSequence value){
        if(value==null || value.isEmpty()){
            return "";
        }
        return value.toString();
    }
}
