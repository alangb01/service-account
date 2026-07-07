package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import org.springframework.http.server.reactive.ServerHttpRequest;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.BaseController;

import java.util.List;

public class ResponseFactory {

    private final BaseController base;

    public ResponseFactory(BaseController base) {
        this.base = base;
    }

    public <T, R> Single<Object> fromMaybe(
            Maybe<T> maybe,
            java.util.function.Function<T, R> mapper,
            String notFoundCode,
            String notFoundMessage,
            ServerHttpRequest request
    ) {

        return maybe
                .map(value ->
                        (Object) base.buildResponse(mapper.apply(value), request)
                )
                .switchIfEmpty(
                        Single.just(
                                (Object) base.buildError(
                                        notFoundCode,
                                        notFoundMessage,
                                        request
                                )
                        )
                )
                .onErrorReturn(error ->
                        (Object) base.buildError(
                                "UNEXPECTED_ERROR",
                                "Unexpected error occurred",
                                request,
                                List.of(error.getMessage())
                        )
                );
    }
}
