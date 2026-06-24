package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.domain.model.CustomerType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ... existing code ...

class AccountControllerTest {

    private final CreateAccountUseCase createAccountUseCase = mock(CreateAccountUseCase.class);
    private final GetAccountUseCase getAccountUseCase = mock(GetAccountUseCase.class);
    private final ListAccountsUseCase listAccountsUseCase = mock(ListAccountsUseCase.class);
    private final UpdateAccountUseCase updateAccountUseCase = mock(UpdateAccountUseCase.class);
    private final DeleteAccountUseCase deleteAccountUseCase = mock(DeleteAccountUseCase.class);
    private final AccountRestMapper accountRestMapper = mock(AccountRestMapper.class);

    private final WebTestClient webTestClient = WebTestClient
            .bindToController(new AccountController(
                    createAccountUseCase,
                    getAccountUseCase,
                    listAccountsUseCase,
                    updateAccountUseCase,
                    deleteAccountUseCase,
                    accountRestMapper
            ))
            .build();


    void createShouldReturnCreatedAccountResponse() {
        CreateAccountRequest request = new CreateAccountRequest(
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN"
        );

        Account createdAccount = account(
                "account-001",
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1000),
                "PEN"
        );

        when(createAccountUseCase.execute(any(Account.class)))
                .thenReturn(Mono.just(createdAccount));

        webTestClient.post()
                .uri("/api/accounts")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("account-001")
                .jsonPath("$.customerId").isEqualTo("customer-001")
                .jsonPath("$.customerType").isEqualTo("PERSONAL")
                .jsonPath("$.number").isEqualTo("ACC-001")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.balance").isEqualTo(1000)
                .jsonPath("$.currency").isEqualTo("PEN")
                .jsonPath("$.active").isEqualTo(true)
                .jsonPath("$.status").isEqualTo("ACTIVE");

        verify(createAccountUseCase).execute(argThat(account ->
                account.id() == null &&
                        account.customerId().equals("customer-001") &&
                        account.customerType() == CustomerType.PERSONAL &&
                        account.number().equals("ACC-001") &&
                        account.type() == AccountType.SAVINGS &&
                        account.balance().compareTo(BigDecimal.valueOf(1000)) == 0 &&
                        account.currency().equals("PEN") &&
                        account.createdAt() != null &&
                        account.active() &&
                        account.status().equals("ACTIVE")
        ));
    }

    @Test
    void updateShouldReturnUpdatedAccountResponse() {
        String id = "account-001";

        UpdateAccountRequest request = new UpdateAccountRequest(
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1500),
                "PEN",
                true,
                "ACTIVE"
        );

        Account updatedAccount = account(
                id,
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1500),
                "PEN"
        );

        when(updateAccountUseCase.execute(eq(id), any(Account.class)))
                .thenReturn(Mono.just(updatedAccount));

        webTestClient.put()
                .uri("/api/accounts/{id}", id)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.customerId").isEqualTo("customer-001")
                .jsonPath("$.customerType").isEqualTo("PERSONAL")
                .jsonPath("$.number").isEqualTo("ACC-001")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.balance").isEqualTo(1500)
                .jsonPath("$.currency").isEqualTo("PEN")
                .jsonPath("$.active").isEqualTo(true)
                .jsonPath("$.status").isEqualTo("ACTIVE");

        verify(updateAccountUseCase).execute(eq(id), argThat(account ->
                account.id().equals(id) &&
                        account.customerId().equals("customer-001") &&
                        account.customerType() == CustomerType.PERSONAL &&
                        account.number().equals("ACC-001") &&
                        account.type() == AccountType.SAVINGS &&
                        account.balance().compareTo(BigDecimal.valueOf(1500)) == 0 &&
                        account.currency().equals("PEN") &&
                        account.createdAt() == null &&
                        account.active() &&
                        account.status().equals("ACTIVE")
        ));
    }


    @Test
    void deleteShouldReturnInactiveAccountResponse() {
        String id = "account-001";

        Account deletedAccount = new Account(
                id,
                "customer-001",
                CustomerType.PERSONAL,
                "ACC-001",
                AccountType.SAVINGS,
                BigDecimal.valueOf(1000),
                "PEN",
                LocalDateTime.now(),
                false,
                "INACTIVE"
        );

        when(deleteAccountUseCase.execute(eq(id)))
                .thenReturn(Mono.just(deletedAccount));

        webTestClient.delete()
                .uri("/api/accounts/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.active").isEqualTo(false)
                .jsonPath("$.status").isEqualTo("INACTIVE");

        verify(deleteAccountUseCase).execute(eq(id));
    }


    private Account account(String id, String customerId, String number, BigDecimal balance, String currency) {
        return new Account(
                id,
                customerId,
                CustomerType.PERSONAL,
                number,
                AccountType.SAVINGS,
                balance,
                currency,
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );
    }
}