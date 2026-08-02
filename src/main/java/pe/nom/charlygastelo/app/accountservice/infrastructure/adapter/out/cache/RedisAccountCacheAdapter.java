package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.out.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import pe.nom.charlygastelo.app.accountservice.domain.model.Account;
import pe.nom.charlygastelo.app.accountservice.domain.port.cache.AccountCachePort;
import pe.nom.charlygastelo.app.accountservice.domain.exception.AccountCacheException;

@RequiredArgsConstructor
public class RedisAccountCacheAdapter implements AccountCachePort {

    private final ReactiveRedisTemplate<String, String> redis;
    private final ObjectMapper mapper;

    private static final String KEY_ID = "account:id:";
    private static final String KEY_DOC = "account:number:";

    @Override
    public Maybe<Account> getById(String id) {
        return Maybe.fromPublisher(
                        redis.opsForValue().get(KEY_ID + id)
                )
                .flatMap(this::parse)
                .onErrorResumeNext(e ->
                        Maybe.error(new AccountCacheException("Redis error", e))
                );
    }

    @Override
    public Maybe<Account> getByNumber(String number) {
        return Maybe.fromPublisher(
                        redis.opsForValue().get(KEY_DOC + number)
                )
                .flatMap(this::parse)
                .onErrorResumeNext(e ->
                        Maybe.error(new AccountCacheException("Redis error", e))
                );
    }


    @Override
    public Completable save(Account account) {
        try {
            String json = mapper.writeValueAsString(account);

            return Completable.fromPublisher(
                    redis.opsForValue().set(KEY_ID + account.id(), json)
            ).andThen(
                    Completable.fromPublisher(
                            redis.opsForValue().set(
                                    KEY_DOC + account.type() + ":" + account.number(),
                                    json
                            )
                    )
            );

        } catch (Exception e) {
            return Completable.error(new AccountCacheException("Error serializing account", e));
        }
    }

    @Override
    public Completable delete(String id) {
        return Completable.fromPublisher(
                redis.opsForValue().delete(KEY_ID + id)
        );
    }

    private Maybe<Account> parse(String json) {
        try {
            return json == null
                    ? Maybe.empty()
                    : Maybe.just(mapper.readValue(json, Account.class));
        } catch (Exception e) {
            return Maybe.error(new AccountCacheException("Error parsing cache", e));
        }
    }
}
