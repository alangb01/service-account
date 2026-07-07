package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolderStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.AddAccountHolderUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.RemoveAccountHolderUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountHolderUseCasePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountHolderCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountHolderResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountHolderRestMapper;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/accounts/{accountId}/holders")
@RequiredArgsConstructor
public class AccountHolderController {

    private final AddAccountHolderUseCasePort addAccountHolderUseCase;
    private final RemoveAccountHolderUseCasePort deleteAccountHolderUseCase;
    private final FindAccountHolderUseCasePort findAccountHolderUseCase;
    private final AccountHolderRestMapper mapper;

    @PostMapping
    public Single<ResponseEntity<AccountHolderResponse>> add(
            @PathVariable String accountId,
            @RequestBody AccountHolderCreateRequest holderRequest,
            ServerHttpRequest request

        ) {

        AccountHolder account = new AccountHolder(
                null,
                accountId,
                holderRequest.customerId(),
                HolderType.valueOf(holderRequest.holderType()),
                Instant.now(),
                null,
                Instant.now(),
                null,
                AccountHolderStatus.ACTIVE
        );

        String token=request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return addAccountHolderUseCase.add(account, token)
                .map(saved ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(mapper.toAccountHolderResponse(saved))
                );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable remove(@PathVariable String id) {
        return deleteAccountHolderUseCase.delete(id);
    }

    @GetMapping
    public Single<List<AccountHolderResponse>> list(@PathVariable String accountId) {
        return findAccountHolderUseCase.findByAccountId(accountId)
                .map(list->list.stream().map(mapper::toAccountHolderResponse).toList());
    }


}