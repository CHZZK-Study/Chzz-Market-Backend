package org.chzz.market.domain.user.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.BusinessException;

public class UserException extends BusinessException {
    public UserException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
