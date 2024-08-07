package org.chzz.market.domain.product.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_REGISTER_FAILED(HttpStatus.BAD_REQUEST, "상품 등록에 실패했습니다."),
    INVALID_PRODUCT_STATE(HttpStatus.BAD_REQUEST, "상품 상태가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
