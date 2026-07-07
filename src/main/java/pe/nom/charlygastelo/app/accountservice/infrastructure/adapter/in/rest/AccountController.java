package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.CreateAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.DeleteAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.UpdateAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountUpdateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.ResponseFactory;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@Slf4j
public class AccountController extends BaseController {

    private final CreateAccountUseCasePort createAccountUseCase;
    private final FindAccountUseCasePort findAccountUseCase;
    private final UpdateAccountUseCasePort updateAccountUseCase;
    private final DeleteAccountUseCasePort deleteAccountUseCase;
    private final AccountRestMapper mapper;
    private final ResponseFactory responseFactory;

    public AccountController(
            CreateAccountUseCasePort createAccountUseCase,
            FindAccountUseCasePort findAccountUseCase,
            UpdateAccountUseCasePort updateAccountUseCase,
            DeleteAccountUseCasePort deleteAccountUseCase,
            AccountRestMapper mapper
    ) {
        this.createAccountUseCase = createAccountUseCase;
        this.findAccountUseCase = findAccountUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.mapper = mapper;

        this.responseFactory = new ResponseFactory(this);
    }

    @PostMapping
    public Single<ResponseEntity<AccountResponse>> create(
            @RequestBody AccountCreateRequest request,
            ServerWebExchange exchange) {

        Account account = mapper.toAccountDomain(request);
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        return createAccountUseCase.create(account, token)
                .map(saved ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(mapper.toAccountResponse(saved))
                );
    }

    @PutMapping("/{id}")
    public Single<ResponseEntity<AccountResponse>> update(
            @PathVariable String id,
            @RequestBody AccountUpdateRequest request) {

        Account account = mapper.toAccountDomain(request);

        return updateAccountUseCase.update(id, account)
                .map(updated ->
                        ResponseEntity.ok(mapper.toAccountResponse(updated))
                );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable delete(@PathVariable String id) {
        return deleteAccountUseCase.delete(id);
    }

    @GetMapping("/{id}")
    public Single<Object> getById(
            @PathVariable String id,
            ServerHttpRequest request
    ) {
        return responseFactory.fromMaybe(
                findAccountUseCase.findById(id),
                mapper::toAccountResponse,
                "ACCOUNT_NOT_FOUND",
                "Account " + id + " not found",
                request
        );
    }


    @GetMapping
    public Single<List<AccountResponse>> list() {
        return findAccountUseCase.findAll().map(accounts ->
                accounts.stream()
                        .map(mapper::toAccountResponse)
                        .toList()
        );
    }
}