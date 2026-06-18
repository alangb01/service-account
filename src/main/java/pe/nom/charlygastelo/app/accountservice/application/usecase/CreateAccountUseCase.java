package pe.nom.charlygastelo.app.accountservice.application.usecase;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Mono;

public class CreateAccountUseCase {

    private final AccountServicePort service;

    public CreateAccountUseCase(AccountServicePort service) {
        this.service = service;
    }

    public Mono<Account> execute(Account account) {
        return service.create(account);
    }
}