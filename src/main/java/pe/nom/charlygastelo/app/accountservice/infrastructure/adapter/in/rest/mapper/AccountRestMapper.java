package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper;

import org.mapstruct.Mapper;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountDetailedResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountUpdateRequest;

@Mapper(componentModel = "spring")
public interface AccountRestMapper {
    AccountResponse toAccountResponse(Account account);
    AccountDetailedResponse toAccountDetailedResponse(Account account);

    Account toAccountDomain(AccountCreateRequest accountRequest);

    Account toAccountDomain(AccountUpdateRequest accountRequest);
}