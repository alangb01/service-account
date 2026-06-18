package pe.nom.charlygastelo.app.accountservice.application.usecase;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class GetAccountUseCase {

    private final AccountServicePort service;

    public GetAccountUseCase(AccountServicePort service) {
        this.service = service;
    }

    public Mono<Account> byId(String id) {
        return service.getById(id);
    }

    public Mono<Account> byNumber(String number) {
        return service.getByNumber(number);
    }
}