package com.restru.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response matching the API specification.
 * Example:
 * {
 *   "error": {
 *     "code": "ITEM_UNAVAILABLE",
 *     "message": "One or more items are no longer available.",
 *     "requestId": "abc-123"
 *   }
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final ErrorBody error;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorBody {
        private final String code;
        private final String message;
        private final String requestId;
        private final Instant timestamp;
        private final Map<String, String> fieldErrors;
    }

    public static ErrorResponse of(String code, String message, String requestId) {
        return ErrorResponse.builder()
                .error(ErrorBody.builder()
                        .code(code)
                        .message(message)
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .build())
                .build();
    }

    public static ErrorResponse withFieldErrors(String code, String message,
                                                 String requestId, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .error(ErrorBody.builder()
                        .code(code)
                        .message(message)
                        .requestId(requestId)
                        .timestamp(Instant.now())
                        .fieldErrors(fieldErrors)
                        .build())
                .build();
    }
}

