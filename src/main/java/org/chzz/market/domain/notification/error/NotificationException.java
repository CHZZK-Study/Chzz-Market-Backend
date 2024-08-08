package org.chzz.market.domain.notification.error;

import org.chzz.market.common.error.BusinessException;
import org.chzz.market.common.error.ErrorCode;

public class NotificationException extends BusinessException {
    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
