package org.chzz.market.common.error.response;

import lombok.Builder;
import org.chzz.market.common.error.ErrorCode;

@Builder
public record ErrorResponse(String code, String message, int status) {
    public static ErrorResponse from(final ErrorCode errorCode){
        return ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .message(errorCode.getMessage())
                .build();
    }
}
