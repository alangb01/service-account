package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountRepositoryPort;
import pe.nom.charlygastelo.app.accountservice.domain.port.AccountServicePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.ReactiveAccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BeanConfigTest {

    @Test
    void shouldCreateApplicationBeans() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestMongoRepositoryConfig.class);
            context.register(BeanConfig.class);
            context.refresh();

            assertThat(context.getBean(AccountRepositoryPort.class)).isNotNull();
            assertThat(context.getBean(AccountServicePort.class)).isNotNull();
            assertThat(context.getBean(CreateAccountUseCase.class)).isNotNull();
            assertThat(context.getBean(GetAccountUseCase.class)).isNotNull();
            assertThat(context.getBean(ListAccountsUseCase.class)).isNotNull();
            assertThat(context.getBean(UpdateAccountUseCase.class)).isNotNull();
            assertThat(context.getBean(DeleteAccountUseCase.class)).isNotNull();

        }
    }

    static class TestMongoRepositoryConfig {

        @Bean
        ReactiveAccountRepository reactiveAccountRepository() {
            return mock(ReactiveAccountRepository.class);
        }
    }
}