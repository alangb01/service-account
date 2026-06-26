package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.exception.AccountRepositoryException;

/**
 * Caso de uso encargado de actualizar la información de una cuenta bancaria.
 */
@RequiredArgsConstructor
@Slf4j
public class CloseAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final AccountCachePort cache;

    /**
     * Actualiza una cuenta bancaria existente.
     *
     * @param id identificador de la cuenta.
     * @param account información actualizada de la cuenta.
     * @return cuenta actualizada.
     */
    public Maybe<Account> execute(String id, Account account) {
        log.info("Starting update process for customer {}", id);
        return accountRepository.findById(id)
                .doOnSuccess(existing ->
                        log.debug("account {} found in MongoDB", id)
                )
                .doOnComplete(() ->
                        log.warn("Account {} not found in MongoDB", id)
                )
                .switchIfEmpty(
                        Single.error(new AccountNotFoundException("Account not found: " + id))
                )
                // Actualizar campos
                .map(existing -> {
                    Account updated = existing.updateWith(account);
                    log.debug("Account {} updated with new data", id);
                    return updated;
                })
                // Guardar en Mongo
                .flatMapMaybe(updated ->
                        accountRepository.save(updated)
                                .doOnSuccess(saved ->
                                        log.info("Account {} updated successfully", id)
                                )
                                .doOnError(e ->
                                        log.error("Error saving updated customer {}: {}", id, e.getMessage(), e)
                                ).flatMap(saved ->
                                        // Publicar evento después de guardar
                                        producer.publishAccountClosed(saved)
                                                .doOnComplete(() ->
                                                        log.info("AccountClosedEvent published for {}", saved.id())
                                                )
                                                .doOnError(e ->
                                                        log.error("Error publishing event for {}: {}", saved.id(), e.getMessage(), e)
                                                )
                                                .andThen(Single.just(saved))
                                )
                                .toMaybe()
                )
                // Error técnico → envolver en excepción de infraestructura
                .onErrorResumeNext(e -> {
                    log.error("Technical error updating customer {}: {}", id, e.getMessage(), e);
                    return Maybe.error(new AccountRepositoryException("Error updating customer", e));
                });

    }

//    private Mono<Void> validateAccountUpdate(Account existingAccount, Account updatedAccount) {
//        if (updatedAccount.customerId() == null || updatedAccount.customerId().isBlank()) {
//            return Mono.error(new IllegalArgumentException("Customer id is required"));
//        }
//
//        if (updatedAccount.type() == null) {
//            return Mono.error(new IllegalArgumentException("Account type is required"));
//        }
//
//        if (!existingAccount.customerId().equals(updatedAccount.customerId())) {
//            return Mono.error(new IllegalArgumentException("Customer id cannot be changed"));
//        }
//
//        if (existingAccount.type() != updatedAccount.type()) {
//            return Mono.error(new IllegalArgumentException("Account type cannot be changed"));
//        }
//
//
//        return Mono.empty();
//    }
}
