package pe.nom.charlygastelo.app.accountservice.application.usecase;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso encargado de desactivar una cuenta bancaria.
 */
public class DeleteAccountUseCase {

    private final AccountServicePort service;

    public DeleteAccountUseCase(AccountServicePort service) {
        this.service = service;
    }

    /**
     * Desactiva una cuenta bancaria mediante borrado lógico.
     *
     * @param id identificador de la cuenta.
     * @return cuenta desactivada.
     */
    public Mono<Account> execute(String id) {
        return service.delete(id);
    }
}
