package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.repository;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolderStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountDocument;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountHolderDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountHolderReactiveRepository
        extends ReactiveMongoRepository<AccountHolderDocument, String> {
    Flux<AccountHolderDocument> findByCustomerId(String customerId);

    Flux<AccountHolderDocument> findByAccountId(String accountId);

    Mono<AccountHolderDocument> findByAccountIdAndCustomerId(String accountId, String customerId);

    Mono<AccountHolderDocument> findByAccountIdAndCustomerIdAndStatus(String accountId, String customerId, AccountHolderStatus accountHolderStatus);

    Mono<AccountHolderDocument> findByAccountIdAndCustomerIdAndHolderType(String accountId, String customerId, HolderType holderType);
}