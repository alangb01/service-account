package pe.nom.charlygastelo.app.accountservice.domain.port;


import pe.nom.charlygastelo.app.accountservice.domain.model.ValidateResponse;
import reactor.core.publisher.Mono;

public interface AuthRepositoryPort {
    Mono<ValidateResponse> validate(String token);
}
