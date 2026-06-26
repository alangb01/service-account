package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.model.AccountType;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.AccountResponse;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.UpdateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountControllerTest {

    private final CreateAccountUseCase createAccountUseCase = mock(CreateAccountUseCase.class);
    private final GetAccountUseCase getAccountUseCase = mock(GetAccountUseCase.class);
    private final ListAccountsUseCase listAccountsUseCase = mock(ListAccountsUseCase.class);
    private final UpdateAccountUseCase updateAccountUseCase = mock(UpdateAccountUseCase.class);
    private final DeleteAccountUseCase deleteAccountUseCase = mock(DeleteAccountUseCase.class);
    private final CloseAccountUseCase closeAccountUseCase = mock(CloseAccountUseCase.class);
    private final AccountRestMapper accountRestMapper = mock(AccountRestMapper.class);

    private final WebTestClient webTestClient = WebTestClient
            .bindToController(new AccountController(
                    createAccountUseCase,
                    getAccountUseCase,
                    listAccountsUseCase,
                    updateAccountUseCase,
                    deleteAccountUseCase,
                    closeAccountUseCase,
                    accountRestMapper
            ))
            .build();

    @Test
    void createShouldReturnCreatedAccountResponse() {
        CreateAccountRequest request = new CreateAccountRequest(
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS.toString(),
                "PEN"
        );

        Account createdAccount = account(
                "account-001",
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1000),
                "PEN"
        );

        AccountResponse response = new AccountResponse(
                "account-001",
                "customer-001",
                "ACC-001",
                "SAVINGS",
                BigDecimal.valueOf(1000),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountRestMapper.toDomain(request)).thenReturn(createdAccount);
        when(createAccountUseCase.execute(any(Account.class))).thenReturn(Single.just(createdAccount));
        when(accountRestMapper.toResponse(createdAccount)).thenReturn(response);

        webTestClient.post()
                .uri("/accounts")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("account-001")
                .jsonPath("$.customerId").isEqualTo("customer-001")
                .jsonPath("$.number").isEqualTo("ACC-001")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.balance").isEqualTo(1000)
                .jsonPath("$.currency").isEqualTo("PEN")
                .jsonPath("$.active").isEqualTo(true)
                .jsonPath("$.status").isEqualTo("ACTIVE");

        verify(accountRestMapper).toDomain(request);
        verify(createAccountUseCase).execute(any(Account.class));
        verify(accountRestMapper).toResponse(createdAccount);
    }

    @Test
    void updateShouldReturnUpdatedAccountResponse() {
        String id = "account-001";

        UpdateAccountRequest request = new UpdateAccountRequest(
                "customer-001",
                "ACC-001",
                AccountType.SAVINGS.toString(),
                BigDecimal.valueOf(1500),
                "PEN",
                null,
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

        AccountResponse response = new AccountResponse(
                id,
                "customer-001",
                "ACC-001",
                "SAVINGS",
                BigDecimal.valueOf(1500),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(accountRestMapper.toDomain(request)).thenReturn(updatedAccount);
        when(updateAccountUseCase.execute(eq(id), any(Account.class))).thenReturn(io.reactivex.rxjava3.core.Maybe.just(updatedAccount));
        when(accountRestMapper.toResponse(updatedAccount)).thenReturn(response);

        webTestClient.put()
                .uri("/accounts/{id}", id)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.customerId").isEqualTo("customer-001")
                .jsonPath("$.number").isEqualTo("ACC-001")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.balance").isEqualTo(1500)
                .jsonPath("$.currency").isEqualTo("PEN")
                .jsonPath("$.active").isEqualTo(true)
                .jsonPath("$.status").isEqualTo("ACTIVE");

        verify(accountRestMapper).toDomain(request);
        verify(updateAccountUseCase).execute(eq(id), any(Account.class));
        verify(accountRestMapper).toResponse(updatedAccount);
    }
    @Test
    void deleteShouldReturnNoContent() {
        String id = "account-001";

        Account account = account(
                id,
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1000),
                "PEN"
        );

        when(getAccountUseCase.byId(id)).thenReturn(io.reactivex.rxjava3.core.Maybe.just(account));
        when(deleteAccountUseCase.execute(eq(id))).thenReturn(io.reactivex.rxjava3.core.Completable.complete());

        webTestClient.delete()
                .uri("/accounts/{id}", id)
                .exchange()
                .expectStatus().isNoContent()  // Cambiado de isOk() a isNoContent()
                .expectBody().isEmpty();  // Opcional pero recomendado para DELETE

        verify(getAccountUseCase).byId(id);
        verify(deleteAccountUseCase).execute(eq(id));
    }


    @Test
    void getByIdShouldReturnAccountResponse() {
        String id = "account-001";

        Account account = account(
                id,
                "customer-001",
                "ACC-001",
                BigDecimal.valueOf(1000),
                "PEN"
        );

        AccountResponse response = new AccountResponse(
                id,
                "customer-001",
                "ACC-001",
                "SAVINGS",
                BigDecimal.valueOf(1000),
                "PEN",
                LocalDateTime.now(),
                true,
                "ACTIVE"
        );

        when(getAccountUseCase.byId(id)).thenReturn(io.reactivex.rxjava3.core.Maybe.just(account));
        when(accountRestMapper.toResponse(account)).thenReturn(response);

        webTestClient.get()
                .uri("/accounts/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.customerId").isEqualTo("customer-001")
                .jsonPath("$.number").isEqualTo("ACC-001");

        verify(getAccountUseCase).byId(id);
        verify(accountRestMapper).toResponse(account);
    }

    @Test
    void listAllShouldReturnAccountsResponse() {
        Account firstAccount = account("account-001", "customer-001", "ACC-001", BigDecimal.valueOf(100), "PEN");
        Account secondAccount = account("account-002", "customer-002", "ACC-002", BigDecimal.valueOf(200), "USD");

        AccountResponse firstResponse = new AccountResponse(
                "account-001", "customer-001", "ACC-001", "SAVINGS", BigDecimal.valueOf(100),
                "PEN", LocalDateTime.now(), true, "ACTIVE"
        );
        AccountResponse secondResponse = new AccountResponse(
                "account-002", "customer-002", "ACC-002", "SAVINGS", BigDecimal.valueOf(200),
                "USD", LocalDateTime.now(), true, "ACTIVE"
        );

        when(listAccountsUseCase.all()).thenReturn(Flowable.just(firstAccount, secondAccount));
        when(accountRestMapper.toResponse(firstAccount)).thenReturn(firstResponse);
        when(accountRestMapper.toResponse(secondAccount)).thenReturn(secondResponse);

        webTestClient.get()
                .uri("/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountResponse.class)
                .hasSize(2);

        verify(listAccountsUseCase).all();
    }

    private Account account(String id, String customerId, String number, BigDecimal balance, String currency) {
        return new Account(
                id,
                customerId,
                number,
                AccountType.SAVINGS,
                balance,
                currency,
                LocalDateTime.now(),
                null,
                null,
                true,
                "ACTIVE"
        );
    }
}
