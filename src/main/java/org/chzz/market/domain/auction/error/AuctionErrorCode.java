package org.chzz.market.domain.auction.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {
    AUCTION_NOT_ACCESSIBLE(BAD_REQUEST, "해당 경매를 조회할 수 없습니다. "),
    AUCTION_ENDED(BAD_REQUEST, "경매가 종료되었습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "경매를 찾을 수 없습니다."),
    INVALID_AUCTION_STATE(BAD_REQUEST, "경매 상태가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
