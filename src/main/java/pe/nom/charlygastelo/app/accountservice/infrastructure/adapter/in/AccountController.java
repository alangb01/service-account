package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pe.nom.charlygastelo.app.accountservice.application.usecase.CreateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.DeleteAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.GetAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ListAccountsUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.UpdateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                             GetAccountUseCase getAccountUseCase,
                             ListAccountsUseCase listAccountsUseCase,
                             UpdateAccountUseCase updateAccountUseCase,
                             DeleteAccountUseCase deleteAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.listAccountsUseCase = listAccountsUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
    }

    @PostMapping
    public Mono<AccountResponse> create(@RequestBody CreateAccountRequest request) {
        Account account = new Account(
                null,
                request.customerId(),
                request.customerType(),
                request.number(),
                request.type(),
                request.initialBalance(),
                request.currency(),
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        return createAccountUseCase.execute(account)
                .map(this::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AccountResponse>> update(@PathVariable String id,
                                                        @RequestBody UpdateAccountRequest request) {
        Account account = new Account(
                id,
                request.customerId(),
                request.customerType(),
                request.number(),
                request.type(),
                request.balance(),
                request.currency(),
                null,
                request.active(),
                request.status()
        );

        return updateAccountUseCase.execute(id, account)
                .map(updatedAccount -> ResponseEntity.ok(toResponse(updatedAccount)))
                .onErrorResume(IllegalArgumentException.class,
                        error -> Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<AccountResponse>> delete(@PathVariable String id) {
        return deleteAccountUseCase.execute(id)
                .map(deletedAccount -> ResponseEntity.ok(toResponse(deletedAccount)))
                .onErrorResume(IllegalArgumentException.class,
                        error -> Mono.just(ResponseEntity.notFound().build()));
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
                a.customerType(),
                a.number(),
                a.type(),
                a.balance(),
                a.currency(),
                a.createdAt(),
                a.active(),
                a.status()
        );
    }
}