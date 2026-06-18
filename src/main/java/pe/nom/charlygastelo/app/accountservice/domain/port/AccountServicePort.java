package pe.nom.charlygastelo.app.accountservice.domain.port;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountServicePort {

    Mono<Account> create(Account account);

    Mono<Account> getById(String id);

    Mono<Account> getByNumber(String number);

    Flux<Account> getByCustomer(String customerId);

    Flux<Account> getAll();
}