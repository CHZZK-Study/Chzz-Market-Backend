package org.chzz.market.domain.payment.error;

import org.chzz.market.common.error.BusinessException;
import org.chzz.market.common.error.ErrorCode;

public class TossPaymentException extends BusinessException {
    private ErrorCode errorCode;
    public TossPaymentException(TossPaymentErrorCode errorCode) {
        super(errorCode);
    }
}
