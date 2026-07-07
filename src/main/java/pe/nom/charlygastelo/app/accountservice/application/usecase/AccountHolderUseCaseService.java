package pe.nom.charlygastelo.app.accountservice.application.usecase;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountHolderNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CustomerClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountHolderRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.AddAccountHolderUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.RemoveAccountHolderUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountHolderUseCasePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.AccountHolderResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountHolderRestMapper;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountHolderUseCaseService implements FindAccountHolderUseCasePort, AddAccountHolderUseCasePort, RemoveAccountHolderUseCasePort {

    private final AccountHolderRepositoryPort accountHolderRepository;
    private final CustomerClientPort customerClient;

    @Override
    public Single<List<AccountHolder>> findByAccountId(String accountId) {
        return accountHolderRepository.findByAccountId(accountId);
    }

    @Override
    public Completable delete(String id) {
        return accountHolderRepository.findById(id)
                .switchIfEmpty(
                        Single.error(
                                new AccountHolderNotFoundException("Account Holder not found with id: " + id)
                        )
                )
                .doOnSuccess(holder ->
                        log.info("AccountHolder {} found, deleting...", id)
                )
                .flatMapCompletable(holder ->
                        accountHolderRepository.delete(holder.id())
                                .doOnComplete(() ->
                                        log.info("AccountHolder {} deleted successfully", id)
                                )
                );
    }

    @Override
    public Single<AccountHolder> add(AccountHolder request, String token) {
        return Single.just(request)
                .flatMap(holder -> validateCustomerExists(holder, token))
                .flatMap(this::validateNoDuplicateHolder)
                .flatMap(this::validateHolderTypeRules)
                .flatMap(accountHolderRepository::save);
    }

    private Single<AccountHolder> validateCustomerExists(AccountHolder holder, String token) {
        return customerClient.getById(holder.customerId(), token)
                .map(customer -> holder);
    }

    private Single<AccountHolder> validateNoDuplicateHolder(AccountHolder holder) {
        return accountHolderRepository.findActiveHolder(
                        holder.accountId(),
                        holder.customerId(),
                        holder.holderType()
                )
                .flatMap(existing ->
                        Maybe.<AccountHolder>error(new BusinessException(
                                "Holder already exists for customer " + holder.customerId()
                        ))
                )
                .switchIfEmpty(Single.just(holder));
    }

    private Single<AccountHolder> validateHolderTypeRules(AccountHolder holder) {

        HolderType type = holder.holderType();

        if (type == HolderType.MINOR) {
            return Single.error(new BusinessException("Minor cannot be OWNER"));
        }

        if (type == HolderType.OWNER) {
            return accountHolderRepository.findOwner(holder.accountId(), holder.customerId())
                    .flatMap(existing ->
                            Maybe.<AccountHolder>error(new BusinessException(
                                    "Account already has an OWNER"
                            ))
                    )
                    .switchIfEmpty(Single.just(holder));
        }

        return Single.just(holder);
    }



}
