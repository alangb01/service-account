package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountLedgerEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.event.AccountManagementEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountDetailedResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountUpdateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.event.mapper.AccountLedgerEventProducerMapper;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountDepositOccurredEvent;
import pe.nom.charlygastelo.app.shared.avro.dto.AccountInitialDepositEvent;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AccountController{

    private final CreateAccountUseCasePort createAccountUseCase;
    private final ListAccountUseCasePort listAccountUseCase;
    private final GetAccountUseCasePort getAccountUseCase;
    private final UpdateAccountUseCasePort updateAccountUseCase;
    private final DeleteAccountUseCasePort deleteAccountUseCase;

    private final AccountRestMapper mapper;

    @GetMapping("/{id}")
    public Maybe<ResponseEntity<AccountDetailedResponse>> getById(
            @PathVariable String id
    ) {
        return getAccountUseCase.findById(id).map(saved->
                ResponseEntity.status(HttpStatus.OK)
                    .body(mapper.toAccountDetailedResponse(saved))
            );
    }


    @GetMapping
    public Single<List<AccountResponse>> list() {
        return listAccountUseCase.findAll().map(accounts ->
                accounts.stream()
                        .map(mapper::toAccountResponse)
                        .toList()
        );
    }

    @PostMapping
    public Single<ResponseEntity<AccountResponse>> create(
            @RequestBody AccountCreateRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {

        Account account = mapper.toAccountDomain(request);
        return createAccountUseCase.create(account, token)
                .map(saved ->
                        ResponseEntity.status(HttpStatus.CREATED)
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


    @GetMapping("/{id}/balance")
    public Single<ResponseEntity<AccountResponse>> balance(
            @PathVariable String id,
            @RequestBody AccountUpdateRequest request) {

        Account account = mapper.toAccountDomain(request);

        return Single.error(new RuntimeException("Not implemented"));
    }
}