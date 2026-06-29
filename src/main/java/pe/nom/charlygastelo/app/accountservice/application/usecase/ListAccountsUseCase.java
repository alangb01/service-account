package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

@Slf4j
@RequiredArgsConstructor
public class ListAccountsUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountCachePort cache;

    // ---------------------------------------------------------
    // LIST ALL ACCOUNTS
    // ---------------------------------------------------------
    public Flowable<Account> all() {

        log.info("[ACCOUNT-LIST] Listing all accounts");

        return accountRepository.findAll()
                .doOnSubscribe(s ->
                        log.debug("[ACCOUNT-LIST] Starting MongoDB findAll()")
                )
                .flatMap(account ->
                        safeCacheSave(account)
                                .andThen(Flowable.just(account))
                )
                .doOnNext(a ->
                        log.debug("[ACCOUNT-LIST] Account loaded: {}", a.id())
                )
                .doOnComplete(() ->
                        log.info("[ACCOUNT-LIST] All accounts loaded successfully")
                )
                .doOnError(e ->
                        log.error("[ACCOUNT-LIST] Error loading accounts: {}", e.getMessage(), e)
                );
    }

    // ---------------------------------------------------------
    // LIST ACCOUNTS BY CUSTOMER
    // ---------------------------------------------------------
    public Flowable<Account> byCustomer(String customerId) {

        log.info("[ACCOUNT-LIST] Listing accounts by customer {}", customerId);

        return accountRepository.findByCustomerId(customerId)
                .doOnSubscribe(s ->
                        log.debug("[ACCOUNT-LIST] Starting MongoDB findByCustomerId()")
                )
                .flatMap(account ->
                        safeCacheSave(account)
                                .andThen(Flowable.just(account))
                )
                .doOnNext(a ->
                        log.debug("[ACCOUNT-LIST] Account by customer loaded: {}", a.id())
                )
                .doOnComplete(() ->
                        log.info("[ACCOUNT-LIST] Accounts by customer {} loaded successfully", customerId)
                )
                .doOnError(e ->
                        log.error("[ACCOUNT-LIST] Error loading accounts: {}", e.getMessage(), e)
                );
    }

    // ---------------------------------------------------------
    // SAFE CACHE SAVE (NO NPE, NO FLOW BREAK)
    // ---------------------------------------------------------
    private Completable safeCacheSave(Account account) {

        Completable op = cache.save(account);

        if (op == null) {
            log.warn("[ACCOUNT-LIST] Cache returned null Completable for accountId={}", account.id());
            return Completable.complete();
        }

        return op
                .doOnComplete(() ->
                        log.debug("[ACCOUNT-LIST] Account cached: {}", account.id())
                )
                .onErrorComplete(e -> {
                    log.warn("[ACCOUNT-LIST] Cache save failed. accountId={}, reason={}",
                            account.id(), e.getMessage());
                    return true;
                });
    }
}
