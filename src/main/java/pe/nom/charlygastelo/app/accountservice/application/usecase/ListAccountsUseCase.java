package pe.nom.charlygastelo.app.accountservice.application.usecase;


import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Slf4j
public class ListAccountsUseCase {

    private final AccountRepositoryPort accountRepository;


    public Flowable<Account> all() {
        log.info("Listing all accounts");
        return accountRepository.findAll()
                .doOnSubscribe(s -> log.debug("Starting MongoDB findAll()"))
                .doOnNext(c -> log.debug("Account loaded: {}", c.id()))
                .doOnComplete(() -> log.info("All accounts loaded successfully"))
                .doOnError(e -> log.error("Error loading accounts: {}", e.getMessage(), e));
    }

    public Flowable<Account> byCustomer(String customerId) {
        log.info("Listing all accounts by customer "+customerId);
        return accountRepository.findByCustomerId(customerId)
                .doOnSubscribe(s -> log.debug("Starting MongoDB findByCustomerId()"))
                .doOnNext(c -> log.debug("Account by customer loaded: {}", c.id()))
                .doOnComplete(() -> log.info("All accounts by customer  loaded successfully"))
                .doOnError(e -> log.error("Error loading accounts: {}", e.getMessage(), e));
    }
}