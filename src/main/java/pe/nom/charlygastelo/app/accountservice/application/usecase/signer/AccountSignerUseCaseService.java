package pe.nom.charlygastelo.app.accountservice.application.usecase.signer;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountHolderNotFoundException;
import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;
import pe.nom.charlygastelo.app.accountservice.domain.model.HolderType;
import pe.nom.charlygastelo.app.accountservice.domain.model.SignerRole;
import pe.nom.charlygastelo.app.accountservice.domain.port.client.CustomerClientPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountHolderRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.repository.AccountSignerRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.AddAccountSignerUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.RemoveAccountSignerUseCasePort;
import pe.nom.charlygastelo.app.accountservice.domain.port.usecase.FindAccountSignerUseCasePort;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSignerUseCaseService implements FindAccountSignerUseCasePort, AddAccountSignerUseCasePort, RemoveAccountSignerUseCasePort {

    private final AccountHolderRepositoryPort accountHolderRepository;
    private final AccountSignerRepositoryPort accountSignerRepository;
    private final CustomerClientPort customerClient;

    @Override
    public Maybe<AccountSigner> findById(String id) {
        return accountSignerRepository.findById(id);
    }

    @Override
    public Single<List<AccountSigner>> findByAccountId(String accountId) {
        return accountSignerRepository.findByAccountId(accountId).toList();
    }

    @Override
    public Completable delete(String id) {
        return accountSignerRepository.findById(id)
                .switchIfEmpty(
                        Single.error(
                                new AccountHolderNotFoundException("Account Signer not found with id: " + id)
                        )
                )
                .doOnSuccess(signer ->
                        log.info("AccountHolder {} found, deleting...", id)
                )
                .flatMapCompletable(signer ->
                        accountSignerRepository.delete(signer.id())
                                .doOnComplete(() ->
                                        log.info("AccountHolder {} deleted successfully", id)
                                )
                );
    }

    @Override
    public Single<AccountSigner> add(AccountSigner signer, String token) {
        return Single.just(signer)
                .flatMap(accountSigner -> validateHolder(signer, token))
                .flatMap(this::validateNoDuplicateSigner)
                .flatMap(this::validateMinorCannotSign)
                .flatMap(accountSignerRepository::save);
    }

    private Single<AccountSigner> validateHolder(AccountSigner accountSigner, String token) {
        return customerClient.getById(accountSigner.customerId(), token)
                .doOnSuccess(existing -> {
                    if (!existing.active()) {
                        throw new AccountHolderNotFoundException("Customer is inactive id: " + accountSigner.customerId());
                    }
                })
                .map(customer -> accountSigner);
    }

    private Single<AccountSigner> validateNoDuplicateSigner(AccountSigner signer) {
        return accountSignerRepository.findActiveSigner(
                        signer.accountId(),
                        signer.customerId(),
                        signer.signerRole()
                )
                .doOnSuccess(existing -> {
                    throw new BusinessException(
                            "Signer already exists for customer " + signer.customerId()
                    );
                })
                .switchIfEmpty(Single.just(signer));
    }

    private Single<AccountSigner> validateMinorCannotSign(AccountSigner signer) {
        return accountHolderRepository.findByAccountIdAndCustomerId(signer.accountId(),signer.customerId())
                .switchIfEmpty(Single.error(
                        new AccountHolderNotFoundException(
                                "Holder not found with id: " + signer.customerId()
                        )
                ))
                .flatMap(holder -> {

                    // Regla 1: menores no pueden firmar
                    if (holder.holderType() == HolderType.MINOR) {
                        return Single.error(new BusinessException(
                                "Minor cannot be signer for account " + signer.accountId()
                        ));
                    }

                    // Regla 2: JOINT no puede ser PRIMARY_SIGNER
                    if (holder.holderType() == HolderType.JOINT &&
                            signer.signerRole() == SignerRole.PRIMARY) {
                        return Single.error(new BusinessException(
                                "Joint holder cannot be PRIMARY signer"
                        ));
                    }

                    // Regla 3: REPRESENTATIVE_OWNER debe ser LEGAL_REPRESENTATIVE
                    if (holder.holderType() == HolderType.REPRESENTATIVE_OWNER &&
                            signer.signerRole() != SignerRole.LEGAL_REPRESENTATIVE) {
                        return Single.error(new BusinessException(
                                "Representative owner must be LEGAL_REPRESENTATIVE signer"
                        ));
                    }

                    return Single.just(signer);
                });
    }


}
