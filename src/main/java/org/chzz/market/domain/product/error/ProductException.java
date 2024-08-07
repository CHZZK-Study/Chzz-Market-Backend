package org.chzz.market.domain.product.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class ProductException extends BusinessException {
    public ProductException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
