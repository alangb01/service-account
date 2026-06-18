package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.nom.charlygastelo.app.accountservice.application.usecase.CreateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.GetAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ListAccountsUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListAccountsUseCase listAccountsUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                             GetAccountUseCase getAccountUseCase,
                             ListAccountsUseCase listAccountsUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.listAccountsUseCase = listAccountsUseCase;
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody CreateAccountRequest request) {
        Account account = new Account(
                null,
                request.customerId(),
                request.number(),
                request.initialBalance(),
                request.currency(),
                LocalDateTime.now(),
                true
        );
        Account created = createAccountUseCase.execute(account);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable String id) {
        return getAccountUseCase.byId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Account>> list(@RequestParam(required = false) String customerId) {
        List<Account> result = (customerId == null)
                ? listAccountsUseCase.all()
                : listAccountsUseCase.byCustomer(customerId);
        return ResponseEntity.ok(result);
    }

    public record CreateAccountRequest(
            String customerId,
            String number,
            BigDecimal initialBalance,
            String currency
    ) {}
}