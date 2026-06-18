package pe.nom.charlygastelo.app.accountservice.domain.port;

import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountServicePort {

    Account create(Account account);

    Optional<Account> getById(String id);

    Optional<Account> getByNumber(String number);

    List<Account> getByCustomer(String customerId);

    List<Account> getAll();
}
