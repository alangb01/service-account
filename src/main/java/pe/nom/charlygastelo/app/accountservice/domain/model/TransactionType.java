package pe.nom.charlygastelo.app.accountservice.domain.model;

public enum TransactionType {
   DEPOSIT,
   WITHDRAWAL,
   TRANSFER, // (entre cuentas del mismo cliente)
   TRANSFER_TO_THIRD,// (si lo manejas aquí)
   ACCOUNT_MAINTENANCE_FEE,
   TRANSACTION_FEE
}
