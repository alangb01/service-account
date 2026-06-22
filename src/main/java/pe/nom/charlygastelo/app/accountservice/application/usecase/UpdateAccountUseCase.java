package pe.nom.charlygastelo.app.accountservice.application.usecase;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso encargado de actualizar la información de una cuenta bancaria.
 */
public class UpdateAccountUseCase {

    private final AccountServicePort service;

    public UpdateAccountUseCase(AccountServicePort service) {
        this.service = service;
    }

    /**
     * Actualiza una cuenta bancaria existente.
     *
     * @param id identificador de la cuenta.
     * @param account información actualizada de la cuenta.
     * @return cuenta actualizada.
     */
    public Mono<Account> execute(String id, Account account) {
        return service.update(id, account);
    }
}
