package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.nom.charlygastelo.app.accountservice.application.usecase.CreateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.GetAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ListAccountsUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import pe.nom.charlygastelo.app.accountservice.domain.service.AccountServiceImpl;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.AccountRepositoryAdapter;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.ReactiveAccountRepository;
@Configuration
public class BeanConfig {

    @Bean
    public AccountRepositoryPort accountRepositoryPort(ReactiveAccountRepository repository) {
        return new AccountRepositoryAdapter(repository);
    }

    @Bean
    public AccountServicePort accountServicePort(AccountRepositoryPort repositoryPort) {
        return new AccountServiceImpl(repositoryPort);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(AccountServicePort servicePort) {
        return new CreateAccountUseCase(servicePort);
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountServicePort servicePort) {
        return new GetAccountUseCase(servicePort);
    }

    @Bean
    public ListAccountsUseCase listAccountsUseCase(AccountServicePort servicePort) {
        return new ListAccountsUseCase(servicePort);
    }
}