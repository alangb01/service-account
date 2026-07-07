package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> {

    private T data;
    private Instant timestamp;
    private String correlationId;
    private String path;
}
