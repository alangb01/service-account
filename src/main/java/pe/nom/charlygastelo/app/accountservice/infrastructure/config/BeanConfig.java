package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import pe.nom.charlygastelo.app.accountservice.application.usecase.CloseAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.CreateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.DeleteAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.GetAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ListAccountsUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.ProcessTransactionUseCase;
import pe.nom.charlygastelo.app.accountservice.application.usecase.UpdateAccountUseCase;
import pe.nom.charlygastelo.app.accountservice.domain.port.*;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.mapper.AccountPersistentMapper;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.AccountRepositoryAdapter;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.persistence.ReactiveAccountRepository;
import pe.nom.charlygastelo.app.accountservice.infrastructure.cache.RedisAccountCacheAdapter;

@Configuration
public class BeanConfig {

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        RedisSerializationContext<String, String> context =
                RedisSerializationContext
                        .<String, String>newSerializationContext(
                                new StringRedisSerializer()
                        )
                        .value(new StringRedisSerializer())
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public AccountCachePort accountCachePort(
            ReactiveRedisTemplate<String, String> redis,
            ObjectMapper mapper) {

        return new RedisAccountCacheAdapter(redis, mapper);
    }

    @Bean
    public AccountRepositoryPort accountRepositoryPort(
            ReactiveAccountRepository repository,
            AccountPersistentMapper mapper) {

        return new AccountRepositoryAdapter(repository, mapper);
    }

    @Bean
    public CreateAccountUseCase createAccountUseCase(
            AccountRepositoryPort repository,
            AccountEventProducerPort producer,
            CustomerEventPort customerEventPort,
            CreditEventPort creditEventPort,
            AccountCachePort cache) {


        return new CreateAccountUseCase(
                repository,
                producer,
                customerEventPort,
                creditEventPort,
                cache
        );
    }

    @Bean
    public GetAccountUseCase getAccountUseCase(
            AccountRepositoryPort repository,
            AccountCachePort cache) {

        return new GetAccountUseCase(repository, cache);
    }

    @Bean
    public ListAccountsUseCase listAccountsUseCase(
            AccountRepositoryPort repository,AccountCachePort cache) {

        return new ListAccountsUseCase(repository, cache);
    }

    @Bean
    public UpdateAccountUseCase updateAccountUseCase(
            AccountRepositoryPort repository,
            AccountEventProducerPort producer,
            AccountCachePort cache) {

        return new UpdateAccountUseCase(
                repository,
                producer,
                cache
        );
    }

    @Bean
    public DeleteAccountUseCase deleteAccountUseCase(
            AccountRepositoryPort repository,
            AccountEventProducerPort producer,
            AccountCachePort cache) {

        return new DeleteAccountUseCase(
                repository,
                producer,
                cache
        );
    }

    @Bean
    public CloseAccountUseCase closeAccountUseCase(
            AccountRepositoryPort repository,
            AccountEventProducerPort producer,
            AccountCachePort cache) {

        return new CloseAccountUseCase(
                repository,
                producer,
                cache
        );
    }


    @Bean
    public ProcessTransactionUseCase processTransactionUseCase(
            AccountRepositoryPort repository,
            MovementEventPort movementEventPort,
            TransactionEventPort transactionEventPort,
            CardEventPort cardEventPort,
            AccountCachePort cache) {

        return new ProcessTransactionUseCase(
                repository,
                cache,
                movementEventPort,
                transactionEventPort,
                cardEventPort
        );
    }
}