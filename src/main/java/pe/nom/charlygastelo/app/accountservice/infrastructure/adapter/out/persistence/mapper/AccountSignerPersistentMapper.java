package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountSigner;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountSignerDocument;

@Component
public class AccountSignerPersistentMapper {

    public AccountSignerDocument toDocument(AccountSigner account) {
        return AccountSignerDocument.builder()
                .id(account.id())
                .accountId(account.accountId())
                .customerId(account.customerId())
                .signerRole(account.signerRole())
                .validFrom(account.validFrom())
                .validTo(account.validTo())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .status(account.status())
                .build();
    }

    public AccountSigner toDomain(AccountSignerDocument document) {
        return new AccountSigner(
                document.getId(),
                document.getAccountId(),
                document.getCustomerId(),
                document.getSignerRole(),
                document.getValidFrom(),
                document.getValidTo(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getStatus()
        );
    }
}