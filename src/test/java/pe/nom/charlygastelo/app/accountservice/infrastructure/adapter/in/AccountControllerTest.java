package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.model.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.mapper.AccountRestMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    private final CreateAccountUseCase createUseCase = mock(CreateAccountUseCase.class);
    private final GetAccountUseCase getUseCase = mock(GetAccountUseCase.class);
    private final ListAccountsUseCase listUseCase = mock(ListAccountsUseCase.class);
    private final UpdateAccountUseCase updateUseCase = mock(UpdateAccountUseCase.class);
    private final DeleteAccountUseCase deleteUseCase = mock(DeleteAccountUseCase.class);
    private final CloseAccountUseCase closeUseCase = mock(CloseAccountUseCase.class);
    private final AccountRestMapper mapper = new AccountRestMapper();

    private final AccountController controller =
            new AccountController(
                    createUseCase,
                    getUseCase,
                    listUseCase,
                    updateUseCase,
                    deleteUseCase,
                    closeUseCase,
                    mapper
            );

    @Test
    void shouldCreateAccount() {
        Account account = account();

        CreateAccountRequest request = new CreateAccountRequest(
                "cus-1",
                "001",
                "SAVINGS",
                "PEN"
        );

        when(createUseCase.execute(any(Account.class)))
                .thenReturn(Single.just(account));

        controller.create(request)
                .test()
                .assertComplete()
                .assertValue(response -> response.getStatusCode().is2xxSuccessful());

        verify(createUseCase).execute(any(Account.class));
    }

    @Test
    void shouldGetAccountById() {
        Account account = account();

        when(getUseCase.byId("acc-1"))
                .thenReturn(Maybe.just(account));

        controller.getById("acc-1")
                .test()
                .assertComplete()
                .assertValue(response -> response.getBody().id().equals("acc-1"));
    }

    @Test
    void shouldListAccounts() {
        when(listUseCase.all())
                .thenReturn(Flowable.just(account()));

        controller.list(null)
                .test()
                .assertValueCount(1)
                .assertComplete();
    }

    @Test
    void shouldDeleteAccount() {
        when(deleteUseCase.execute("acc-1"))
                .thenReturn(Completable.complete());

        controller.delete("acc-1")
                .test()
                .assertComplete();

        verify(deleteUseCase).execute("acc-1");
    }

    @Test
    void shouldCloseAccount() {
        Account closed = account().close();

        when(closeUseCase.execute("acc-1"))
                .thenReturn(Maybe.just(closed));

        controller.close("acc-1")
                .test()
                .assertComplete()
                .assertValue(response ->
                        "CLOSED".equals(response.getBody().status())
                );
    }

    private Account account() {
        return new Account(
                "acc-1",
                "cus-1",
                "001",
                AccountType.SAVINGS,
                BigDecimal.TEN,
                "PEN",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                true,
                AccountStatus.ACTIVE
        );
    }
}