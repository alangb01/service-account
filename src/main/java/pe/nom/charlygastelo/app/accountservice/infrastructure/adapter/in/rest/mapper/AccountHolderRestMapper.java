package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper;

import org.mapstruct.Mapper;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountHolderCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountHolderResponse;

@Mapper(componentModel = "spring")
public interface AccountHolderRestMapper {
    AccountHolderResponse toAccountHolderResponse(AccountHolder account);

    AccountHolder toAccountHolderDomain(AccountHolderCreateRequest accountRequest);
}