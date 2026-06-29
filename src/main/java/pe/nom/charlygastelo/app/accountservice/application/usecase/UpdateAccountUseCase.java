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
@RequiredArgsConstructor
@Slf4j
public class UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final AccountCachePort cache;

    public Maybe<Account> execute(String id, Account account) {

        log.info("[ACCOUNT-UPDATE] Updating account {}", id);

        return accountRepository.findById(id)
                .doOnComplete(() ->
                        log.warn("[ACCOUNT-UPDATE] Account not found. accountId={}", id)
                )
                .switchIfEmpty(
                        Single.error(new AccountNotFoundException("Account not found: " + id))
                )

                // Update fields
                .map(existing -> {
                    Account updated = existing.updateWith(account);
                    log.debug("[ACCOUNT-UPDATE] Account updated in memory. accountId={}", id);
                    return updated;
                })

                // Save in Mongo
                .flatMap(accountRepository::save)
                .doOnSuccess(saved ->
                        log.info("[ACCOUNT-UPDATE] Account saved in MongoDB. accountId={}", saved.id())
                )
                .doOnError(e ->
                        log.error("[ACCOUNT-UPDATE] Error saving account. accountId={}, reason={}",
                                id, e.getMessage(), e)
                )

                // Update cache
                .flatMap(saved ->
                        cache.delete(id)
                                .doOnComplete(() ->
                                        log.debug("[ACCOUNT-UPDATE] Old cache evicted. accountId={}", id)
                                )
                                .onErrorComplete(e -> {
                                    log.warn("[ACCOUNT-UPDATE] Cache eviction failed. accountId={}, reason={}",
                                            id, e.getMessage());
                                    return true;
                                })
                                .andThen(
                                        cache.save(saved)
                                                .doOnComplete(() ->
                                                        log.info("[ACCOUNT-UPDATE] Account cached successfully. accountId={}", id)
                                                )
                                                .onErrorComplete(e -> {
                                                    log.warn("[ACCOUNT-UPDATE] Cache save failed. accountId={}, reason={}",
                                                            id, e.getMessage());
                                                    return true;
                                                })
                                )
                                .andThen(Single.just(saved))
                )

                // Publish event
                .flatMap(saved ->
                        producer.publishAccountUpdated(saved)
                                .doOnComplete(() ->
                                        log.info("[ACCOUNT-UPDATE] AccountUpdatedEvent published. accountId={}", id)
                                )
                                .doOnError(e ->
                                        log.error("[ACCOUNT-UPDATE] Error publishing AccountUpdatedEvent. accountId={}, reason={}",
                                                id, e.getMessage(), e)
                                )
                                .andThen(Single.just(saved))
                )

                .toMaybe()

                // Final logs
                .doOnSuccess(saved ->
                        log.info("[ACCOUNT-UPDATE] Account updated successfully. accountId={}", saved.id())
                )
                .doOnError(error ->
                        log.error("[ACCOUNT-UPDATE] Error updating account. accountId={}, reason={}",
                                id, error.getMessage(), error)
                );
    }
}
