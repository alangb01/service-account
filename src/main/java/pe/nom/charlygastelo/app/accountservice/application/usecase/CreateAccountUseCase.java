package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountLimitExceededException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.CustomerNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountEventProducerPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.CustomerClientPort;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class CreateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final AccountEventProducerPort producer;
    private final CustomerClientPort customerClient;

    public Single<Account> execute(Account account) {

        log.info("Starting account creation process for customer {}", account.customerId());

        return customerClient.getById(account.customerId())

                .flatMap(customer ->
                        accountRepository.findByCustomerIdAndType(
                                        account.customerId(),
                                        account.type().toString()
                                )
                                .isEmpty() // Flowable<Account> → Single<Boolean>
                                .doOnSuccess(isEmpty ->
                                        log.debug("Existence check for {} returned {}",
                                                customer.documentNumber(), !isEmpty)
                                )
                                .flatMap(isEmpty -> {
                                    if (!isEmpty) { // si NO está vacío → ya existe una cuenta
                                        return Single.error(new AccountLimitExceededException(
                                                "El cliente ya tiene una cuenta de este tipo"
                                        ));
                                    }
                                    return Single.just(account);
                                })
                )

                // Guardar cuenta (Single<Account>)
                .flatMap(accountRepository::save)

                // Publicar evento (Completable → Single<Account>)
                .flatMap(saved ->
                        producer.publishAccountCreated(saved)
                                .doOnComplete(() ->
                                        log.info("AccountCreatedEvent published for {}", saved.number())
                                )
                                .doOnError(e ->
                                        log.error("Error publishing event for {}: {}",
                                                saved.number(), e.getMessage(), e)
                                )
                                .andThen(Single.just(saved))
                );


    }


}