package pe.nom.charlygastelo.app.accountservice.application.usecase;


import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;

import java.util.List;

public class ListAccountsUseCase {

    private final AccountServicePort service;

    public ListAccountsUseCase(AccountServicePort service) {
        this.service = service;
    }

    public List<Account> all() {
        return service.getAll();
    }

    public List<Account> byCustomer(String customerId) {
        return service.getByCustomer(customerId);
    }
}