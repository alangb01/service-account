package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountReactiveRepository
        extends ReactiveMongoRepository<AccountDocument, String> {

    Mono<AccountDocument> findByNumber(String number);

    Flux<AccountDocument> findByCustomerId(String customerId);

    Flux<AccountDocument> findByCustomerIdAndType(String customerId, String type);
}