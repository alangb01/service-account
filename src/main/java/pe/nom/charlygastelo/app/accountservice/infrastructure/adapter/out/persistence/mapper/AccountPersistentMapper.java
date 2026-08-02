package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.mapper;

import org.springframework.stereotype.Component;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountStatus;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.document.AccountDocument;

import java.util.ArrayList;

@Component
public class AccountPersistentMapper {

    public Account toDomain(AccountDocument document) {
        return new Account(
                document.getId(),
                document.getCustomerId(),
                document.getNumber(),
                AccountType.valueOf(document.getType()),
                document.getBalance(),
                document.getAvailable(),
                document.getFreeTransactionsLimit(),
                document.getMinimumOpeningAmount(),
                document.getMonthlyTransactionCount(),
                document.getCommissionAmount(),
                document.getCurrency(),
                new ArrayList<>(),
                new ArrayList<>(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getClosedAt(),
                document.isActive(),
                document.getStatus()
        );
    }

    public AccountDocument toCreateDocument(Account account) {
        return AccountDocument.builder()
                .id(account.id())
                .customerId(account.customerId())
                .number(account.number())
                .type(account.type() == null ? null : account.type().name())
                .balance(account.balance())
                .available(account.available())
                .freeTransactionsLimit(account.freeTransactionLimit())
                .minimumOpeningAmount(account.minimumOpeningAmount())
                .monthlyTransactionCount(account.monthlyTransactionCount())
                .commissionAmount(account.commisionAmount())
                .currency(account.currency())
                .createdAt(account.createdAt())
                .updatedAt(account.updatedAt())
                .closedAt(account.closedAt())
                .active(account.active())
                .status(account.status() == null ? null : account.status())
                .build();
    }
}