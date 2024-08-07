package org.chzz.market.domain.payment.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    INVALID_METHOD(HttpStatus.BAD_REQUEST, "결제 수단이 옳지 않습니다."),
    ALREADY_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "이미 존재하는 orderId 입니다."),
    CREATION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "orderId 생성에 실패했습니다. 다시 시도해주세요.");
    private final HttpStatus httpStatus;
    private final String message;
}
