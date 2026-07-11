package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;

import java.util.List;

@RestController
@RequestMapping("/customers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CustomerController {
    private final FindAccountUseCasePort listAccountsUseCase;
    private final AccountRestMapper restMapper;

    @GetMapping("/{id}/accounts")
    public Single<List<AccountResponse>> listByCustomer(
            @PathVariable String id) {

        return listAccountsUseCase.findByCustomerId(id)
                .map(list->list.stream().map(restMapper::toAccountResponse).toList());
    }
}