package org.chzz.market.domain.auction.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {
    AUCTION_NOT_ACCESSIBLE(HttpStatus.BAD_REQUEST, "해당 경매를 조회할 수 없습니다. ");

    private final HttpStatus httpStatus;
    private final String message;
}
