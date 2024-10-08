package org.chzz.market.domain.product.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_REGISTER_FAILED(HttpStatus.BAD_REQUEST, "상품 등록에 실패했습니다."),
    INVALID_PRODUCT_STATE(HttpStatus.BAD_REQUEST, "상품 상태가 유효하지 않습니다."),
    ALREADY_IN_AUCTION(HttpStatus.BAD_REQUEST, "이미 경매가 진행 중인 상품입니다."),
    PRODUCT_ALREADY_AUCTIONED(HttpStatus.BAD_REQUEST, "상품이 이미 경매로 등록되어 삭제할 수 없습니다."),
    FORBIDDEN_PRODUCT_ACCESS(HttpStatus.FORBIDDEN, "상품에 접근할 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 이미지를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND_OR_IN_AUCTION(HttpStatus.NOT_FOUND, "상품을 찾을 수 없거나 경매 상태입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
