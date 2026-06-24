package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.exception.AccountRepositoryException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class GetAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountCachePort cache;

    public Maybe<Account> byId(String id) {
        log.info("Searching customer by ID {}", id);
        return cache.getById(id)
                .doOnSuccess(c -> log.info("Customer {} found in cache", id))
                .doOnComplete(() -> log.debug("Customer {} not found in cache", id))

                .switchIfEmpty(
                        accountRepository.findById(id)
                                .doOnSuccess(c -> log.info("Customer {} found in MongoDB", id))
                                .doOnError(e -> log.error("Error accessing MongoDB for {}", id, e))
                                .flatMap(account ->
                                        cache.save(account)
                                                .doOnComplete(() -> log.debug("Customer {} cached", id))
                                                .andThen(Maybe.just(account))
                                )
                )

                .switchIfEmpty(
                        Maybe.error(new AccountNotFoundException("Customer not found: " + id))
                );
    }

    public Maybe<Account> byNumber(String type,String number) {
        log.info("Searching customer by document {} {}", type, number);

        return cache.getByNumber(number)
                .doOnSuccess(c -> log.info("Customer {}-{} found in cache", type, number))
                .doOnComplete(() -> log.debug("Customer {}-{} not found in cache", type, number))

                .switchIfEmpty(
                        accountRepository.findByNumber(number)
                                .doOnSuccess(c -> log.info("Customer {}-{} found in MongoDB", type, number))
                                .doOnError(e -> log.error("Error accessing MongoDB for {}-{}", type, number, e))
                                .flatMap(customer ->
                                        cache.save(customer)
                                                .doOnComplete(() -> log.debug("Customer {}-{} cached", type, number))
                                                .andThen(Maybe.just(customer))
                                )
                )

                .onErrorResumeNext(e -> {
                    log.error("Technical error retrieving customer {}-{}: {}", type, number, e.getMessage(), e);
                    return Maybe.error(new AccountRepositoryException("Error accessing Mongo", e));
                })

                .switchIfEmpty(
                        Maybe.error(new AccountNotFoundException(
                                "Customer not found with " + type + " " + number
                        ))
                );
    }

}