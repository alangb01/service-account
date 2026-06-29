package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.AccountDocument;

@Component
public class AccountPersistentMapper {

    public AccountDocument toDocument(Account account) {
        return AccountDocument.builder()
                .id(account.id())
                .customerId(account.customerId())
                .customerType(account.customerType())
                .number(account.number())
                .type(account.type() == null ? null : account.type().name())
                .balance(account.balance())
                .currency(account.currency())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .closedAt(account.closedAt())
                .active(account.active())
                .status(account.status() == null ? null : account.status().name())
                .build();
    }

    public Account toDomain(AccountDocument document) {
        return new Account(
                document.getId(),
                document.getCustomerId(),
                document.getCustomerType(),
                document.getNumber(),
                AccountType.valueOf(document.getType()),
                document.getBalance(),
                document.getCurrency(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getClosedAt(),
                document.isActive(),
                AccountStatus.valueOf(document.getStatus())
        );
    }
}