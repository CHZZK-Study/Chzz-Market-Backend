package org.chzz.market.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    UNSUPPORTED_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "Unsupported type of parameter included"),
    UNSUPPORTED_PARAMETER_NAME(HttpStatus.BAD_REQUEST, "Unsupported name of parameter included"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not exists"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "external api error. check server log.");

    private final HttpStatus httpStatus;
    private final String message;
}