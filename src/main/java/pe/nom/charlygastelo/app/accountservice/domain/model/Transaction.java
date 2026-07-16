package pe.nom.charlygastelo.app.accountservice.domain.model;

import pe.nom.charlygastelo.app.accountservice.domain.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String id,
        String customerId,
        String sourceProductType,
        String targetProductType,
        String sourceProductId,
        String targetProductId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal commission,
        String description,
        Instant timestamp
) {

    private void validateAmount() {
        if (this.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
    }

    public void validateForWithdraw() {
        if (this.type!=TransactionType.WITHDRAW) {
            throw new BusinessException("Invalid transaction type for withdraw");
        }

        validateAmount();
    }

    public void validateForDeposit() {
        if (this.type!=TransactionType.DEPOSIT) {
            throw new BusinessException("Invalid transaction type for deposit");
        }
        validateAmount();
    }

    public void validateForCreditPayment() {
        if (this.type!=TransactionType.CREDIT_PAYMENT ) {
            throw new BusinessException("Invalid transaction type for credit payment");
        }
        validateAmount();
    }


    public void validateForDebitCardPayment() {
        if (this.type!=TransactionType.DEBIT_CARD_PAYMENT ) {
            throw new BusinessException("Invalid transaction type for debit card payment");
        }
        validateAmount();
    }

    public void validateForDebitCardPurchase() {
        if (this.type!=TransactionType.DEBIT_CARD_PURCHASE ) {
            throw new BusinessException("Invalid transaction type for debit card payment");
        }
        validateAmount();
    }

    public void validateForYankiPayment() {
        if (this.type!=TransactionType.YANKI_PAYMENT ) {
            throw new BusinessException("Invalid transaction type for yanki payment");
        }
        validateAmount();
    }
}