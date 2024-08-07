package org.chzz.market.domain.bid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ENDED;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BELOW_MIN_PRICE;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BY_OWNER;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_LIMIT_EXCEEDED;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.Auction.AuctionStatus;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {
    private static final String ERROR_CODE = "errorCode";

    @Mock
    private AuctionService auctionService;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BidService bidService;

    private BidCreateRequest bidCreateRequest;
    private User user, user2;
    private Product product, product2, product3;
    private Auction auction, completeAuction, endAuction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).providerId("1234").nickname("닉네임1").email("asd@naver.com").build();
        user2 = User.builder().id(2L).providerId("12345").nickname("닉네임2").email("asd@naver.com").build();
        product = Product.builder().id(1L).user(user).name("제품1").category(Category.FASHION_AND_CLOTHING).build();
        product2 = Product.builder().id(2L).user(user).name("제품2").category(Category.FASHION_AND_CLOTHING).build();
        product3 = Product.builder().id(3L).user(user).name("제품3").category(Category.FASHION_AND_CLOTHING).build();
        auction = Auction.builder().id(1L).product(product).minPrice(1000).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        completeAuction = Auction.builder().id(2L).product(product2).minPrice(1000).status(AuctionStatus.ENDED)
                .endDateTime(LocalDateTime.now().minusDays(1)).build();
        endAuction = Auction.builder().id(3L).product(product3).minPrice(1000).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().minusDays(1)).build();
    }

    @Test
    @DisplayName("성공 - 처음 입찰 한 경우")
    public void firstBid_Success() throws Exception {
        //given
        bidCreateRequest = BidCreateRequest.builder().auctionId(1L).amount(1000L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(auction);
        when(bidRepository.findByAuctionAndBidder(auction, user2)).thenReturn(Optional.empty());

        //when & then
        Assertions.assertDoesNotThrow(() -> bidService.createBid(bidCreateRequest, 2L));
    }

    @Test
    @DisplayName("성공 - 이미 입찰 한 경우 업데이트")
    public void updateBid_Success() throws Exception {
        //given
        bidCreateRequest = BidCreateRequest.builder().auctionId(1L).amount(2000L).build();
        Bid bid = Bid.builder().id(1L).auction(auction).bidder(user2).amount(1000L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(auction);
        when(bidRepository.findByAuctionAndBidder(auction, user2)).thenReturn(Optional.of(bid));

        //when
        bidService.createBid(bidCreateRequest, 2L);

        //then
        assertThat(bid.getId()).isEqualTo(1L);
        assertThat(bid.getAmount()).isEqualTo(2000L);
        assertThat(bid.getCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("실패 - 경매 등록자가 입찰할 때 예외 발생")
    public void ownerBid_ThrowsException() throws Exception {
        // given
        bidCreateRequest = BidCreateRequest.builder().auctionId(1L).amount(1000L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(auction);

        // when & then
        assertThatThrownBy(() -> bidService.createBid(bidCreateRequest, 1L))
                .isInstanceOf(BidException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(BID_BY_OWNER);
    }

    @Test
    @DisplayName("실패 - 경매가 상태가 진행이 아닐 때 예외 발생")
    public void notProceeding_ThrowsException() throws Exception {
        //given
        bidCreateRequest = BidCreateRequest.builder().auctionId(2L).amount(1000L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(completeAuction);

        //when & then
        assertThatThrownBy(() -> bidService.createBid(bidCreateRequest, 2L))
                .isInstanceOf(AuctionException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(AUCTION_ENDED);

    }

    @Test
    @DisplayName("실패 - 입찰 시각이 종료시각을 지날 때 예외 발생")
    public void auctionEnded_ThrowsException() throws Exception {
        //given
        bidCreateRequest = BidCreateRequest.builder().auctionId(3L).amount(1000L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(endAuction);

        //when & then
        assertThatThrownBy(() -> bidService.createBid(bidCreateRequest, 2L))
                .isInstanceOf(AuctionException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(AUCTION_ENDED);
    }

    @Test
    @DisplayName("실패 - 최소 금액보다 낮은 입찰 금액일때 예외 발생")
    public void bidBelowMinPrice_ThrowsException() throws Exception {
        //given
        bidCreateRequest = BidCreateRequest.builder().auctionId(1L).amount(500L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(auction);

        //when & then
        assertThatThrownBy(() -> bidService.createBid(bidCreateRequest, 2L))
                .isInstanceOf(BidException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(BID_BELOW_MIN_PRICE);
    }

    @Test
    @DisplayName("실패 - 남은 입찰 횟수가 0보다 작을 때 입찰 한 경우 예외 발생")
    public void bidCountZeroOrLess_ThrowsException() throws Exception {
        //given
        Bid bid = Bid.builder().id(1L).auction(auction).bidder(user2).amount(1000L).count(0).build();
        bidCreateRequest = BidCreateRequest.builder().auctionId(1L).amount(5000L).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(auctionService.getAuction(bidCreateRequest.getAuctionId())).thenReturn(auction);
        when(bidRepository.findByAuctionAndBidder(auction, user2)).thenReturn(Optional.of(bid));

        //when & then
        assertThatThrownBy(() -> bidService.createBid(bidCreateRequest, 2L))
                .isInstanceOf(BidException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(BID_LIMIT_EXCEEDED);
    }

}
