package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSignerStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.SignerRole;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.request.AccountSignerCreateRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountSignerResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountSignerRestMapper;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts/{accountId}/signers")
@RequiredArgsConstructor
public class AccountSignerController {

    private final AddAccountSignerUseCasePort addAccountSignerUseCase;
    private final RemoveAccountSignerUseCasePort deleteAccountSignerUseCase;
    private final FindAccountSignerUseCasePort findAccountSignerUseCase;
    private final AccountSignerRestMapper mapper;

    @PostMapping
    public Single<ResponseEntity<AccountSignerResponse>> add(
            @PathVariable String accountId,
            @RequestBody AccountSignerCreateRequest signerRequest,
            ServerHttpRequest request
        ) {

        AccountSigner account = new AccountSigner(
            null,
                accountId,
                signerRequest.customerId(),
                SignerRole.valueOf(signerRequest.signerRole()),
                Instant.now(),
                null,
                Instant.now(),
                null,
                AccountSignerStatus.ACTIVE
        );

        String token=request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return addAccountSignerUseCase.add(account, token)
                .map(saved ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(mapper.toAccountSignerResponse(saved))
                );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable remove(@PathVariable String id) {
        return deleteAccountSignerUseCase.delete(id);
    }

    @GetMapping
    public Single<List<AccountSignerResponse>> list(@PathVariable String accountId) {
        return findAccountSignerUseCase.findByAccountId(accountId)
                .map(list->list.stream().map(mapper::toAccountSignerResponse)
                        .collect(Collectors.toList()));
    }


}