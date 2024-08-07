package org.chzz.market.domain.auction.dto.response;

import static org.chzz.market.common.util.TimeUtil.calculateSecondsUntilEnd;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.chzz.market.domain.auction.entity.Auction.Status;

/**
 * 나의 경매 목록 조회 DTO
 */
@Getter
@ToString
public class MyAuctionResponse {
    private Long id;
    private String name;
    private String cdnPath;
    private Long timeRemaining;
    private Integer minPrice;
    private Long participantCount;
    private Status status;
    private LocalDateTime createdAt;

    @QueryProjection
    public MyAuctionResponse(Long id, String name, String cdnPath, LocalDateTime endDateTime, Integer minPrice,
                             Long participantCount, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.cdnPath = cdnPath;
        this.timeRemaining = calculateSecondsUntilEnd(endDateTime);
        this.minPrice = minPrice;
        this.participantCount = participantCount;
        this.status = status;
        this.createdAt = createdAt;
    }
}
