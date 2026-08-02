package pe.nom.charlygastelo.app.accountservice.application.usecase.account;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountHolderRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountSignerRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.DeleteAccountUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.UpdateAccountUseCasePort;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateAccountUseCase implements UpdateAccountUseCasePort{

    private final AccountRepositoryPort accountRepository;


    @Override
    public Single<Account> update(String id, Account request) {
        return accountRepository.findById(id)
                .switchIfEmpty(Single.error(new BusinessException("Account not found.")))
                .flatMap(existing -> {
                    Account updated = existing.updateWith(request);
                    return accountRepository.save(updated);
                })
                .doOnSuccess(updated -> log.info("Account updated successfully. accountId={}", updated.id()))
                .doOnError(error -> log.error("Error occurred while updating account. accountId={}", id, error));
    }
}
