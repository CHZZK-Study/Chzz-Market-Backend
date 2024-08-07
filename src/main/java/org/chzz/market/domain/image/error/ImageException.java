package org.chzz.market.domain.image.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.BusinessException;

public class ImageException extends BusinessException {
    public ImageException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
