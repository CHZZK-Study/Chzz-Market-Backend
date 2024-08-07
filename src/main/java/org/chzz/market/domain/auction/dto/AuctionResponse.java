package org.chzz.market.domain.auction.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;

/**
 * 진행중인 경매 목록 조회 DTO
 */
@Getter
public class AuctionResponse {
    private Long id;
    private String name;
    private String cdnPath;
    private Long timeRemaining;
    private Integer minPrice;
    private Long participantCount;
    private Boolean isParticipating;

    @QueryProjection
    public AuctionResponse(Long id, String name, String cdnPath, LocalDateTime endDateTime, Integer minPrice,
                           Long participantCount,
                           Boolean isParticipating) {
        this.id = id;
        this.name = name;
        this.cdnPath = cdnPath;
        this.timeRemaining = calculateTimeRemaining(endDateTime);
        this.minPrice = minPrice;
        this.participantCount = participantCount;
        this.isParticipating = isParticipating;
    }

    /**
     * 경매 종료까지 남은 시간 계산
     *
     * @param endDateTime
     * @return 초 단위로 남은 시간
     */
    private Long calculateTimeRemaining(LocalDateTime endDateTime) {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), endDateTime);
    }
}
