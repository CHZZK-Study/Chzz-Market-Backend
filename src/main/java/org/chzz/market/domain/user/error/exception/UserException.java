package org.chzz.market.domain.user.error.exception;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class UserException extends BusinessException {
    public UserException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
