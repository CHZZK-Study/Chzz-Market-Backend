package org.chzz.market.domain.address.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class AddressException extends BusinessException {
    public AddressException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
