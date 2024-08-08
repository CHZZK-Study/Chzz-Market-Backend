package org.chzz.market.domain.notification.error;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    REDIS_MESSAGE_SEND_FAILURE(INTERNAL_SERVER_ERROR, "Redis 메시지 발신에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
