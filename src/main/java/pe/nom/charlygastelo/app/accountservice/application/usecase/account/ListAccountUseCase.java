package pe.nom.charlygastelo.app.accountservice.application.usecase.account;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountHolderRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountSignerRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.ListAccountUseCasePort;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListAccountUseCase implements ListAccountUseCasePort {

    private final AccountRepositoryPort accountRepository;
    private final AccountSignerRepositoryPort accountSignerRepository;
    private final AccountHolderRepositoryPort accountHolderRepository;

    @Override
    public Single<List<Account>> findAll() {

        log.debug("Finding all accounts.");
        return accountRepository.findAll()
                .toList();
    }

    @Override
    public Single<List<Account>> findByCustomerId(String customerId) {
        log.debug("Finding accounts by customer id. customerId={}", customerId);
        return accountRepository.findByCustomerId(customerId)
                .toList();
    }
}
