package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;

@RequiredArgsConstructor
@Slf4j
public class GetAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountCachePort cache;

    public Maybe<Account> byId(String id) {
        log.info("Searching account by id={}", id);

        return cache.getById(id)
                .doOnSuccess(account ->
                        log.info("Account found in cache. accountId={}", id))
                .onErrorResumeNext(error -> {
                    log.warn("Redis get by id failed. Falling back to MongoDB. accountId={}, reason={}",
                            id, error.getMessage());
                    return Maybe.empty();
                })
                .switchIfEmpty(
                        Maybe.defer(() ->
                                accountRepository.findById(id)
                                        .doOnSuccess(account ->
                                                log.info("Account found in MongoDB. accountId={}", id))
                                        .flatMap(account ->
                                                cache.save(account)
                                                        .onErrorComplete(cacheError -> {
                                                            log.warn(
                                                                    "Redis save failed. Continuing. accountId={}, reason={}",
                                                                    account.id(),
                                                                    cacheError.getMessage()
                                                            );
                                                            return true;
                                                        })
                                                        .andThen(Maybe.just(account))
                                        )
                        )
                )
                .switchIfEmpty(Maybe.error(
                        new AccountNotFoundException("Account not found: " + id)
                ))
                .doOnError(error ->
                        log.error("Error searching account by id. accountId={}, reason={}",
                                id, error.getMessage(), error));
    }

    public Maybe<Account> byNumber(String type, String number) {
        log.info("Searching account by number. type={}, number={}", type, number);

        return cache.getByNumber(number)
                .doOnSuccess(account ->
                        log.info("Account found in cache. number={}", number))
                .onErrorResumeNext(error -> {
                    log.warn("Redis get by number failed. Falling back to MongoDB. number={}, reason={}",
                            number, error.getMessage());
                    return Maybe.empty();
                })
                .switchIfEmpty(
                        Maybe.defer(() ->
                                accountRepository.findByNumber(number)
                                        .doOnSuccess(account ->
                                                log.info("Account found in MongoDB. number={}", number))
                                        .flatMap(account ->
                                                cache.save(account)
                                                        .onErrorComplete(cacheError -> {
                                                            log.warn(
                                                                    "Redis save failed. Continuing. accountId={}, reason={}",
                                                                    account.id(),
                                                                    cacheError.getMessage()
                                                            );
                                                            return true;
                                                        })
                                                        .andThen(Maybe.just(account))
                                        )
                        )
                )
                .switchIfEmpty(Maybe.error(
                        new AccountNotFoundException(
                                "Account not found with " + type + " " + number
                        )
                ))
                .doOnError(error ->
                        log.error("Error searching account by number. number={}, reason={}",
                                number, error.getMessage(), error));
    }
}