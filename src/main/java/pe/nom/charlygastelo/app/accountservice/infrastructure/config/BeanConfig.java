package pe.nom.charlygastelo.app.accountservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import pe.nom.charlygastelo.app.accountservice.domain.port.cache.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.cache.RedisAccountCacheAdapter;

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

}