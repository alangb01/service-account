package pe.nom.charlygastelo.app.accountservice.domain.port;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {

    Mono<Account> save(Account account);

    Mono<Account> findById(String id);

    Mono<Account> findByNumber(String number);

    Flux<Account> findByCustomerId(String customerId);

    Flux<Account> findAll();
}
