package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, String> {

    Optional<AccountEntity> findByNumber(String number);

    List<AccountEntity> findByCustomerId(String customerId);
}