package pe.nom.charlygastelo.app.accountservice.domain.model;

public enum TransactionType {
   DEPOSIT,
   WITHDRAW,
   TRANSFER, // (entre cuentas del mismo cliente)
   TRANSFER_TO_THIRD,// (si lo manejas aquí)
//   ACCOUNT_MAINTENANCE_FEE,
   TRANSACTION_FEE,
   CREDIT_PAYMENT,
   DEBIT_CARD_PAYMENT,
   YANKI_PAYMENT,
}
