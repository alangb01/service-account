package pe.nom.charlygastelo.app.accountservice.domain.model;

public enum TransactionType {
   DEPOSIT, // ->ACCOUNT
   WITHDRAW, // ACCOUNT ->
   TRANSFER, // ACCOUNT -> ACCOUNT mismo customer destino
   TRANSFER_TO_THIRD,// ACCOUNT - ACCOUNT diferente customer destino

   CREDIT_PAYMENT,   // ACCOUNT - CREDIT
   DEBIT_CARD_PAYMENT, //ACCOUNT - ACCOUNT
   CREDIT_CARD_PAYMENT, //ACCOUNT - CREDIT CARD
   YANKI_PAYMENT,

   OTHER, DEBIT_CARD_PURCHASE;



}
