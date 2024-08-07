package org.chzz.market.domain.payment.error;

import org.chzz.market.common.error.exception.BusinessException;
import org.chzz.market.common.error.ErrorCode;

public class PaymentException extends BusinessException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
