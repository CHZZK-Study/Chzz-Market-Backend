package org.chzz.market.domain.image.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드를 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제를 실패했습니다. "),
    INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 확장자입니다."),
    MAX_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 5개까지 등록할 수 있습니다."),
    INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "이미지는 개수가 올바르지 않습니다."),
    NO_IMAGES_PROVIDED(HttpStatus.BAD_REQUEST, "이미지가 제공되지 않았습니다."),
    NOT_FOUND(HttpStatus.BAD_REQUEST, "이미지가 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
