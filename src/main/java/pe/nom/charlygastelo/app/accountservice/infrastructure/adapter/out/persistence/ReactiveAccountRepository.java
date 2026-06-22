package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveAccountRepository
        extends ReactiveMongoRepository<AccountDocument, String> {

    Mono<AccountDocument> findByNumber(String number);

    Flux<AccountDocument> findByCustomerId(String customerId);
}