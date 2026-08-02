package pe.nom.charlygastelo.app.accountservice.domain.model;

import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Account(
        String id,
        String customerId,
        String number,
        AccountType type,

        BigDecimal balance,
        BigDecimal available,

        Integer freeTransactionLimit,
        BigDecimal  minimumOpeningAmount,
        Integer monthlyTransactionCount,
        BigDecimal commisionAmount,

        String currency,

        List<AccountHolder> holders,
        List<AccountSigner> signers,

        Instant createdAt,
        Instant updatedAt,
        Instant closedAt,
        boolean active,
        AccountStatus status
) {


    public Account updateWith(Account account) {
        return new Account(
                id,
                customerId,
                number,
                account.type() == null ? type : account.type(),
                account.balance() == null ? balance : account.balance(),
                account.available() == null ? available : account.available(),
                freeTransactionLimit,
                minimumOpeningAmount,
                monthlyTransactionCount,
                commisionAmount,
                account.currency() == null ? currency : account.currency(),
                holders,
                signers,
                createdAt,
                Instant.now(),
                closedAt,
                active,
                status
        );
    }

    public Account createWith(Account account) {
        return new Account(
                id,
                customerId,
                number,
                account.type() == null ? type : account.type(),
                account.balance() == null ? balance : account.balance(),
                account.balance() == null ? balance : account.balance(),
                freeTransactionLimit,
                minimumOpeningAmount,
                monthlyTransactionCount,
                commisionAmount,

                account.currency() == null ? currency : account.currency(),
                holders,
                signers,
                Instant.now(),
               null,
                null,
                true,
                AccountStatus.ACTIVE
        );
    }

    public Account close() {
        return new Account(
                id,
                customerId,
                number,
                type,
                balance,
                available,
                freeTransactionLimit,
                minimumOpeningAmount,
                monthlyTransactionCount,
                commisionAmount,

                currency,
                holders,
                signers,
                createdAt,
                Instant.now(),
                Instant.now(),
                false,
                AccountStatus.CLOSED
        );
    }

    public boolean isActive() {
        return active && status == AccountStatus.ACTIVE;
    }


    public boolean hasEnoughBalance(BigDecimal amount) {
        if (amount == null || balance == null) {
            return false;
        }

        // Monto mínimo permitido para retiros
        if (amount.compareTo(new BigDecimal("0.01")) < 0) {
            return false;
        }

        // Validación de saldo suficiente
        return balance.compareTo(amount) >= 0;
    }

    public Account debit(BigDecimal amount) {

        if (this.available.compareTo(amount) < 0) {
            throw new BusinessException("Insufficient available balance");
        }

        BigDecimal newBalance = this.balance.subtract(amount);
        BigDecimal newAvailable = this.available.subtract(amount);

        int newMonthlyCount;

        if (this.monthlyTransactionCount == null) {
            newMonthlyCount = 0;
        } else {
            newMonthlyCount = this.monthlyTransactionCount;
        }

        newMonthlyCount++;

        BigDecimal commission = BigDecimal.ZERO;

        if (this.freeTransactionLimit != null &&
                newMonthlyCount > this.freeTransactionLimit &&
                this.commisionAmount != null) {

            commission = this.commisionAmount;

            if (newAvailable.compareTo(commission) < 0) {
                throw new BusinessException("Insufficient available balance for commission");
            }

            newBalance = newBalance.subtract(commission);
            newAvailable = newAvailable.subtract(commission);
        }

        return new Account(
                this.id,
                this.customerId,
                this.number,
                this.type,
                newBalance,
                newAvailable,
                this.freeTransactionLimit,
                minimumOpeningAmount,
                newMonthlyCount,
                commission,              // comisión aplicada en esta operación
                this.currency,
                this.holders,
                this.signers,
                this.createdAt,
                Instant.now(),           // updatedAt
                this.closedAt,
                this.active,
                this.status
        );
    }

    public Account credit(BigDecimal amount) {

        BigDecimal newBalance = this.balance.add(amount);
        BigDecimal newAvailable = this.available.add(amount);

        int newMonthlyCount;

        if (this.monthlyTransactionCount == null) {
            newMonthlyCount = 0;
        } else {
            newMonthlyCount = this.monthlyTransactionCount;
        }

        newMonthlyCount++;

        return new Account(
                this.id,
                this.customerId,
                this.number,
                this.type,
                newBalance,
                newAvailable,
                this.freeTransactionLimit,
                minimumOpeningAmount,
                newMonthlyCount,
                BigDecimal.ZERO,         // depósitos no generan comisión
                this.currency,
                this.holders,
                this.signers,
                this.createdAt,
                Instant.now(),
                this.closedAt,
                this.active,
                this.status
        );
    }



//    private Account updateAmount(BigDecimal newAmount) {
//        return new Account(
//                id,
//                customerId,
//                number,
//                type,
//                newAmount,
//                freeTransactionLimit,
//                monthlyTransactionCount,
//                commisionAmount,
//
//                currency,
//                holders,
//                signers,
//                createdAt,
//                Instant.now(),
//                closedAt,
//                active,
//                status
//        );
//    }

    public Account updateHoldersSigners(List<AccountHolder> holders, List<AccountSigner> signers) {
        System.out.println(holders);
        System.out.println(signers);
        return new Account(
                id,
                customerId,
                number,
                type,
                balance,
                available,
                freeTransactionLimit,
                minimumOpeningAmount,
                monthlyTransactionCount,
                commisionAmount,

                currency,
                holders,
                signers,
                createdAt,
                updatedAt,
                closedAt,
                active,
                status
        );
    }

}