package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;

@RestController
@RequestMapping("/customers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CustomerController {
//    private final ListAccountsUseCase listAccountsUseCase;
    private final AccountRestMapper restMapper;

//    @GetMapping("/{id}/accounts")
//    public Flowable<AccountResponse> listByCustomer(
//            @PathVariable String id) {
//
//        return listAccountsUseCase.byCustomer(id)
//                .map(restMapper::toResponse);
//    }
}