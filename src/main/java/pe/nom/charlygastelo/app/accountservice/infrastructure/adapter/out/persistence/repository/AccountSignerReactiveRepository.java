package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSignerStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.SignerRole;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountSignerDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountSignerReactiveRepository
        extends ReactiveMongoRepository<AccountSignerDocument, String> {

    Flux<AccountSignerDocument> findByAccountId(String accountId);
    Flux<AccountSignerDocument> findByCustomerId(String customerId);

    Mono<AccountSignerDocument> findByAccountIdAndCustomerIdAndSignerRoleAndStatus(
            String accountId, String customerId, SignerRole signerRole, AccountSignerStatus accountStatus
    );
}