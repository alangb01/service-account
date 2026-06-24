package pe.nom.charlygastelo.app.accountservice.infrastructure.clients.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.clients.dto.CustomerResponse;

@Component
public class CustomerClientMapper {

    public Customer toDomain(CustomerResponse response) {
       return new Customer(
               response.id(),
               response.customerType(),
               response.documentType(),
               response.documentNumber(),
               response.name(),
               response.fullName(),
               response.email(),
               response.phone(),
               response.active()
       );
    }
}
