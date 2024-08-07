package org.chzz.market.domain.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_ACCESSIBLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuctionServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("경매 상세 조회 - 값이 채워진 경우 예외 발생 안함")
    public void testGetAuctionDetails_ExistingAuction_NoException() {
        // given
        Long existingAuctionId = 1L;
        Long userId = 1L;
        AuctionDetailsResponse auctionDetails = new AuctionDetailsResponse(1L, 2L, "닉네임2", "제품1", null, 1000,
                LocalDateTime.now().plusDays(1), Auction.Status.PROCEEDING, false, 0L, false, 0L, 0);

        // when
        when(auctionRepository.findAuctionDetailsById(anyLong(), anyLong())).thenReturn(Optional.of(auctionDetails));

        // then
        assertDoesNotThrow(() -> {
            auctionService.getAuctionDetails(existingAuctionId, userId);
        });
    }

    @Test
    @DisplayName("경매 상세 조회 - 빈 값이 리턴 되는 경우 예외 발생")
    public void testGetAuctionDetails_NonExistentAuction() {
        // given
        Long nonExistentAuctionId = 999L;
        Long userId = 1L;

        // when
        when(auctionRepository.findAuctionDetailsById(anyLong(), anyLong())).thenReturn(Optional.empty());

        // then
        AuctionException auctionException = assertThrows(AuctionException.class, () -> {
            auctionService.getAuctionDetails(nonExistentAuctionId, userId);
        });
        assertThat(auctionException.getErrorCode()).isEqualTo(AUCTION_NOT_ACCESSIBLE);
    }

}
