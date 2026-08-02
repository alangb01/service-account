package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client.mapper;

import org.mapstruct.Mapper;
import pe.nom.charlygastelo.app.accountservice.domain.model.Customer;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.client.dto.CustomerResponse;


@Mapper(componentModel = "spring")
public interface CustomerClientMapper {
     Customer toCustomerDomain(CustomerResponse response);
}
