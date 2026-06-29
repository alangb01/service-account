package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

@RequiredArgsConstructor
@Slf4j
public class DeleteAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final AccountCachePort cache;

    public Completable execute(String id) {

        log.info("[ACCOUNT-DELETE] Request received to delete account {}", id);

        return accountRepository.findById(id)
                .doOnComplete(() ->
                        log.warn("[ACCOUNT-DELETE] Account not found. accountId={}", id)
                )
                .switchIfEmpty(
                        Maybe.error(new AccountNotFoundException("Account not found: " + id))
                )
                .toSingle()

                // Delete in Mongo
                .flatMapCompletable(account ->
                        accountRepository.deleteById(id)
                                .doOnComplete(() ->
                                        log.info("[ACCOUNT-DELETE] Account deleted from MongoDB. accountId={}", id)
                                )

                                // Evict cache
                                .andThen(cache.delete(id)
                                        .doOnComplete(() ->
                                                log.info("[ACCOUNT-DELETE] Account cache evicted. accountId={}", id)
                                        )
                                        .onErrorComplete(e -> {
                                            log.warn("[ACCOUNT-DELETE] Cache eviction failed. accountId={}, reason={}",
                                                    id, e.getMessage());
                                            return true;
                                        })
                                )

                                // Publish event
                                .andThen(
                                        producer.publishAccountDeleted(id)
                                                .doOnComplete(() ->
                                                        log.info("[ACCOUNT-DELETE] AccountDeletedEvent published. accountId={}", id)
                                                )
                                                .doOnError(e ->
                                                        log.error("[ACCOUNT-DELETE] Error publishing AccountDeletedEvent. accountId={}, reason={}",
                                                                id, e.getMessage(), e)
                                                )
                                )
                )

                .doOnComplete(() ->
                        log.info("[ACCOUNT-DELETE] Account deletion process completed successfully. accountId={}", id)
                )
                .doOnError(error ->
                        log.error("[ACCOUNT-DELETE] Error deleting account. accountId={}, reason={}",
                                id, error.getMessage(), error)
                );
    }
}
