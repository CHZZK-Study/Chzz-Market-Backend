package org.chzz.market.domain.auction.error;

import org.chzz.market.common.error.BusinessException;
import org.chzz.market.common.error.ErrorCode;

public class AuctionException extends BusinessException {
    public AuctionException(ErrorCode errorCode) {
        super(errorCode);
    }

}
