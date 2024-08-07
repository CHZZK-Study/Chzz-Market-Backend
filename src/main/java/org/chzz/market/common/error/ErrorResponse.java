package org.chzz.market.common.error;

import lombok.Builder;

@Builder
public record ErrorResponse(String message,
                            int status) {
    public static ErrorResponse from(final ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .build();
    }
}