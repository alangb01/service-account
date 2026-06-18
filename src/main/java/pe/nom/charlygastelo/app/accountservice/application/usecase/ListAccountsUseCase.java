package pe.nom.charlygastelo.app.accountservice.application.usecase;


import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Flux;


public class ListAccountsUseCase {

    private final AccountServicePort service;

    public ListAccountsUseCase(AccountServicePort service) {
        this.service = service;
    }

    public Flux<Account> all() {
        return service.getAll();
    }

    public Flux<Account> byCustomer(String customerId) {
        return service.getByCustomer(customerId);
    }
}