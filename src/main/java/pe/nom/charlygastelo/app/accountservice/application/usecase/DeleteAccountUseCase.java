package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import reactor.core.publisher.Mono;

/**
 * Caso de uso encargado de desactivar una cuenta bancaria.
 */
@RequiredArgsConstructor
@Slf4j
public class DeleteAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountCachePort cache;

    /**
     * Desactiva una cuenta bancaria mediante borrado lógico.
     *
     * @param id identificador de la cuenta.
     * @return cuenta desactivada.
     */
    public Completable execute(String id) {
        log.info("Request received to delete customer {}", id);
        return accountRepository.existsById(id)
                .doOnSuccess(exists -> log.info("Checking existence for customer {}", id))
                .flatMapCompletable(exists -> {
                    if (!exists) {
                        log.warn("Customer {} not found", id);
                        return Completable.error(new AccountNotFoundException("Customer not found: " + id));
                    }

                    log.info("Deleting customer {}", id);
                    return accountRepository.deleteById(id)
                            .andThen(cache.delete(id))
                            .doOnComplete(() -> log.info("Customer {} deleted", id));
                });
    }
}
