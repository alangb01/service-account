package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

/**
 * Caso de uso encargado de desactivar una cuenta bancaria.
 */
@RequiredArgsConstructor
@Slf4j
public class DeleteAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final AccountCachePort cache;

    /**
     * Desactiva una cuenta bancaria mediante borrado lógico.
     *
     * @param id identificador de la cuenta.
     * @return cuenta desactivada.
     */
    public Completable execute(String id) {
        log.info("Request received to delete customer {}", id);
        return accountRepository.findById(id)
                // Log si existe
                .doOnSuccess(acc ->
                        log.info("Account {} found in MongoDB", id)
                )
                // Si no existe → error de dominio
                .switchIfEmpty(
                        Maybe.error(new AccountNotFoundException("Account not found: " + id))
                )

                // Convertimos Maybe<Account> → Single<Account>
                .toSingle()

                // Usamos el account, pero retornamos Completable
                .flatMapCompletable(account ->
                        accountRepository.deleteById(id)
                                .andThen(cache.delete(id))
                                .doOnComplete(() ->
                                        log.info("Account {} deleted from DB and cache", id)
                                )
                                // Publicar evento usando el Account
                                .andThen(
                                        producer.publishAccountDeleted(account)
                                                .doOnComplete(() ->
                                                        log.info("AccountDeletedEvent published for {}", account.id())
                                                )
                                                .doOnError(e ->
                                                        log.error("Error publishing AccountDeletedEvent for {}: {}",
                                                                account.id(), e.getMessage(), e)
                                                )
                                )
                );

    }
}
