package pe.nom.charlygastelo.app.accountservice.application.usecase.account;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountHolderRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountSignerRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.GetAccountUseCasePort;


@Component
@RequiredArgsConstructor
@Slf4j
public class GetAccountUseCase implements GetAccountUseCasePort {

    private final AccountRepositoryPort accountRepository;
    private final AccountSignerRepositoryPort accountSignerRepository;
    private final AccountHolderRepositoryPort accountHolderRepository;

    @Override
    public Maybe<Account> findById(String id) {
        log.debug("Finding account by id. id={}", id);

        return accountRepository.findById(id)
            .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found")))
            .flatMap(account ->
                Single.zip(
                    accountHolderRepository.findByAccountId(account.id()).toList(),
                    accountSignerRepository.findByAccountId(account.id()).toList(),
                    account::updateHoldersSigners
                ).toMaybe()
            )

            .doOnSuccess(account -> {
                log.info("Account found. id={}", account.id());
            })
            .doOnError(error -> {
                log.error("Error occurred while fetching account. id={}", id, error);
            });
    }

    @Override
    public Maybe<Account> requestById(String id, String correlationId) {
        return null;
    }

}
