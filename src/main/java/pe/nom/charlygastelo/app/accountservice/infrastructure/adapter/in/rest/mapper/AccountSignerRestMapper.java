package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper;

import org.mapstruct.Mapper;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountSignerCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountSignerResponse;

@Mapper(componentModel = "spring")
public interface AccountSignerRestMapper {
    AccountSignerResponse toAccountSignerResponse(AccountSigner account);

    AccountSigner toAccountSignerDomain(AccountSignerCreateRequest accountRequest);
}