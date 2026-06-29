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
public class CloseAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final AccountCachePort cache;

    public Maybe<Account> execute(String id) {

        log.info("[ACCOUNT-CLOSE] Closing account. accountId={}", id);

        return accountRepository.findById(id)
                .doOnComplete(() ->
                        log.warn("[ACCOUNT-CLOSE] Account not found. accountId={}", id)
                )
                .switchIfEmpty(
                        Single.error(new AccountNotFoundException("Account not found: " + id))
                )

                // Close account
                .map(existing -> {
                    Account closed = existing.close();
                    log.debug("[ACCOUNT-CLOSE] Account state changed to CLOSED. accountId={}", id);
                    return closed;
                })

                // Save in Mongo
                .flatMap(accountRepository::save)
                .doOnSuccess(saved ->
                        log.info("[ACCOUNT-CLOSE] Account saved in MongoDB. accountId={}", saved.id())
                )
                .doOnError(e ->
                        log.error("[ACCOUNT-CLOSE] Error saving account. accountId={}, reason={}",
                                id, e.getMessage(), e)
                )

                // Update cache
                .flatMap(saved ->
                        cache.delete(id)
                                .doOnComplete(() ->
                                        log.debug("[ACCOUNT-CLOSE] Old cache evicted. accountId={}", id)
                                )
                                .onErrorComplete(e -> {
                                    log.warn("[ACCOUNT-CLOSE] Cache eviction failed. accountId={}, reason={}",
                                            id, e.getMessage());
                                    return true;
                                })
                                .andThen(cache.save(saved)
                                        .doOnComplete(() ->
                                                log.info("[ACCOUNT-CLOSE] Account cached successfully. accountId={}", id)
                                        )
                                        .onErrorComplete(e -> {
                                            log.warn("[ACCOUNT-CLOSE] Cache save failed. accountId={}, reason={}",
                                                    id, e.getMessage());
                                            return true;
                                        })
                                )
                                .andThen(Single.just(saved))
                )

                // Publish event
                .flatMap(saved ->
                        producer.publishAccountClosed(saved)
                                .doOnComplete(() ->
                                        log.info("[ACCOUNT-CLOSE] AccountClosedEvent published. accountId={}", id)
                                )
                                .doOnError(e ->
                                        log.error("[ACCOUNT-CLOSE] Error publishing AccountClosedEvent. accountId={}, reason={}",
                                                id, e.getMessage(), e)
                                )
                                .andThen(Single.just(saved))
                )

                .toMaybe()

                // Final logs
                .doOnSuccess(saved ->
                        log.info("[ACCOUNT-CLOSE] Account closed successfully. accountId={}", saved.id())
                )
                .doOnError(error ->
                        log.error("[ACCOUNT-CLOSE] Error closing account. accountId={}, reason={}",
                                id, error.getMessage(), error)
                );
    }
}
