package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ApiError {

    private String error;
    private String message;
    private Instant timestamp;
    private String correlationId;
    private String path;
    private List<String> details;
}
