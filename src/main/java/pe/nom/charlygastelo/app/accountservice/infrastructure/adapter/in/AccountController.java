package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.application.usecase.CloseAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.CreateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.DeleteAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.GetAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ListAccountsUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.UpdateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final CloseAccountUseCase closeAccountUseCase;
    private final AccountRestMapper restMapper;

    @PostMapping
    public Single<ResponseEntity<AccountResponse>> create(
            @RequestBody CreateAccountRequest request) {

        Account account = restMapper.toDomain(request);

        return createAccountUseCase.execute(account)
                .map(saved ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(restMapper.toResponse(saved))
                );
    }

    @PutMapping("/{id}")
    public Single<ResponseEntity<AccountResponse>> update(
            @PathVariable String id,
            @RequestBody UpdateAccountRequest request) {

        Account account = restMapper.toDomain(request);

        return updateAccountUseCase.execute(id, account)
                .map(updated ->
                        ResponseEntity.ok(restMapper.toResponse(updated))
                )
                .toSingle();
    }

    @PatchMapping("/{id}/close")
    public Single<ResponseEntity<AccountResponse>> close(
            @PathVariable String id) {

        return closeAccountUseCase.execute(id)
                .map(closed ->
                        ResponseEntity.ok(restMapper.toResponse(closed))
                )
                .toSingle();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable delete(@PathVariable String id) {
        return deleteAccountUseCase.execute(id);
    }

    @GetMapping("/{id}")
    public Single<ResponseEntity<AccountResponse>> getById(
            @PathVariable String id) {

        return getAccountUseCase.byId(id)
                .map(account ->
                        ResponseEntity.ok(restMapper.toResponse(account))
                )
                .toSingle();
    }

    @GetMapping
    public Flowable<AccountResponse> list(
            @RequestParam(required = false) String customerId) {

        return (customerId == null
                ? listAccountsUseCase.all()
                : listAccountsUseCase.byCustomer(customerId))
                .map(restMapper::toResponse);
    }
}