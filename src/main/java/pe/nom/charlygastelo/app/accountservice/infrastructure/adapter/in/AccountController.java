package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.application.usecase.CreateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.GetAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ListAccountsUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListAccountsUseCase listAccountsUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                             GetAccountUseCase getAccountUseCase,
                             ListAccountsUseCase listAccountsUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.listAccountsUseCase = listAccountsUseCase;
    }

    @PostMapping
    public Mono<AccountResponse> create(@RequestBody CreateAccountRequest request) {
        Account account = new Account(
                null,
                request.customerId(),
                request.number(),
                request.initialBalance(),
                request.currency(),
                LocalDateTime.now(),
                true
        );

        return createAccountUseCase.execute(account)
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AccountResponse>> getById(@PathVariable String id) {
        return getAccountUseCase.byId(id)
                .map(a -> ResponseEntity.ok(toResponse(a)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<AccountResponse> list(@RequestParam(required = false) String customerId) {
        return (customerId == null
                ? listAccountsUseCase.all()
                : listAccountsUseCase.byCustomer(customerId))
                .map(this::toResponse);
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.id(),
                a.customerId(),
                a.number(),
                a.balance(),
                a.currency(),
                a.createdAt(),
                a.active()
        );
    }
}