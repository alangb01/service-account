package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountHolder;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountHolderDocument;

@Component
public class AccountHolderPersistentMapper {

    public AccountHolderDocument toDocument(AccountHolder account) {
        return AccountHolderDocument.builder()
                .id(account.id())
                .customerId(account.customerId())
                .accountId(account.accountId())
                .holderType(account.holderType())
                .validFrom(account.validFrom())
                .validTo(account.validTo())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .status(account.status())
                .build();
    }

    public AccountHolder toDomain(AccountHolderDocument document) {
        return new AccountHolder(
                document.getId(),
                document.getAccountId(),
                document.getCustomerId(),
                document.getHolderType(),
                document.getValidFrom(),
                document.getValidTo(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getStatus()
        );
    }
}