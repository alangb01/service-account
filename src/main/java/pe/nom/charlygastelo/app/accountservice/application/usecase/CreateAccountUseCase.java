package pe.nom.charlygastelo.app.accountservice.application.usecase;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;

public class CreateAccountUseCase {

    private final AccountServicePort service;

    public CreateAccountUseCase(AccountServicePort service) {
        this.service = service;
    }

    public Account execute(Account account) {
        return service.create(account);
    }
}