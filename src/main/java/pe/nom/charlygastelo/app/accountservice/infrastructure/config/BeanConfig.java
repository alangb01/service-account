package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import pe.nom.charlygastelo.app.accountservice.application.usecase.*;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper.AccountPersistentMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.AccountRepositoryAdapter;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.ReactiveAccountRepository;
import pe.nom.charlygastelo.app.accountservice.infrastructure.cache.RedisAccountCacheAdapter;

@Configuration
public class    BeanConfig {

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        RedisSerializationContext<String, String> context =
                RedisSerializationContext.<String, String>newSerializationContext(
                        new StringRedisSerializer()
                ).value(new StringRedisSerializer()).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public AccountCachePort customerCachePort(
            ReactiveRedisTemplate<String, String> redis,
            ObjectMapper mapper) {

        return new RedisAccountCacheAdapter(redis, mapper);
    }

    @Bean
    public AccountRepositoryPort accountRepositoryPort(ReactiveAccountRepository repository, AccountPersistentMapper mapper) {
        return new AccountRepositoryAdapter(repository,mapper);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(AccountRepositoryPort servicePort,
                                                     AccountEventProducerPort eventPublisher,
                                                     CustomerEventPort customerClient
                                                     ) {
        return new CreateAccountUseCase(servicePort, eventPublisher,customerClient);
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(AccountRepositoryPort repositoryPort,AccountCachePort cache) {
        return new GetAccountUseCase(repositoryPort, cache);
    }

    @Bean
    public ListAccountsUseCase listAccountsUseCase(AccountRepositoryPort repositoryPort) {
        return new ListAccountsUseCase(repositoryPort);
    }


    @Bean
    public UpdateAccountUseCase updateAccountUseCase(AccountRepositoryPort repositoryPort, AccountEventProducerPort producer) {
        return new UpdateAccountUseCase(repositoryPort,producer);
    }

    @Bean
    public DeleteAccountUseCase deleteAccountUseCase(AccountRepositoryPort repositoryPort, AccountCachePort cache, AccountEventProducerPort producer) {
        return new DeleteAccountUseCase(repositoryPort, producer, cache);
    }

    @Bean
    public CloseAccountUseCase closeAccountUseCase(AccountRepositoryPort repositoryPort, AccountCachePort cache, AccountEventProducerPort producer) {
        return new CloseAccountUseCase(repositoryPort, producer, cache);
    }
}