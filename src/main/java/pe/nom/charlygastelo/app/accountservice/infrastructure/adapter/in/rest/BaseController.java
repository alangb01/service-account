package pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest;

import org.springframework.http.server.reactive.ServerHttpRequest;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.ApiError;
import pe.nom.charlygastelo.app.accountservice.infrastructure.adapter.in.rest.dto.response.ApiResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public abstract class BaseController {
    public <T> ApiResponse<T> buildResponse(T data, ServerHttpRequest request) {
        return ApiResponse.<T>builder()
                .data(data)
                .timestamp(Instant.now())
                .correlationId(generateCorrelationId())
                .path(request.getPath().value())
                .build();
    }

    public ApiError buildError(String errorCode,
                               String message,
                               ServerHttpRequest request,
                               List<String> details) {

        return ApiError.builder()
                .error(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .correlationId(generateCorrelationId())
                .path(request.getPath().value())
                .details(details)
                .build();
    }

    public ApiError buildError(String errorCode,
                               String message,
                               ServerHttpRequest request) {

        return buildError(errorCode, message, request, List.of());
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
