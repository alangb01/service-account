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

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteAccountUseCase implements DeleteAccountUseCasePort {

    private final AccountRepositoryPort accountRepository;
    private final AccountSignerRepositoryPort accountSignerRepository;
    private final AccountHolderRepositoryPort accountHolderRepository;


    @Override
    public Completable delete(String id) {
        log.info("Deleting account. accountId={}", id);
        return accountRepository.findById(id)
                .switchIfEmpty(Single.error(new BusinessException("Account not found.")))
                .flatMapCompletable(account -> accountRepository.deleteById(id))
                .doOnComplete(() -> log.info("Account deleted successfully. accountId={}", id));
    }
}
