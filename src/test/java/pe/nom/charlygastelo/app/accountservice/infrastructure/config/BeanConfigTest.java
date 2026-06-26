package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.ReactiveAccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BeanConfigTest {

    @Test
    void shouldCreateUseCaseBeans() {
        // Crear mocks para las dependencias
        AccountRepositoryPort mockRepo = mock(AccountRepositoryPort.class);
        AccountEventProducerPort mockProducer=mock(AccountEventProducerPort.class);
        CustomerEventPort mockClient=mock(CustomerEventPort.class);
        AccountCachePort mockCache=mock(AccountCachePort.class);
        // Test individual usecases with their dependencies mocked
        assertThat(new CreateAccountUseCase(mockRepo, mockProducer, mockClient)).isNotNull();
        assertThat(new GetAccountUseCase(mockRepo, mockCache)).isNotNull();
        assertThat(new ListAccountsUseCase(mockRepo)).isNotNull(); // Corregido: eliminada coma extra
        assertThat(new UpdateAccountUseCase(mockRepo,mockProducer)).isNotNull();
        assertThat(new DeleteAccountUseCase(mockRepo,mockProducer,mockCache)).isNotNull();
    }
}
