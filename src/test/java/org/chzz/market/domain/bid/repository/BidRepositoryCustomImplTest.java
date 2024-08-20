package org.chzz.market.domain.bid.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.Auction.Status;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.entity.User.UserRole;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DatabaseTest
class BidRepositoryCustomImplTest {

    @Autowired
    BidRepository bidRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuctionRepository auctionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private User bidder1;
    private User bidder2;
    private Auction auction1, auction2;

    @BeforeEach
    void setUp() {
        bidder1 = User.builder()
                .nickname("bidder1")
                .providerType(ProviderType.KAKAO)
                .email("aaa11@gmail.com")
                .userRole(UserRole.USER)
                .providerId("12314")
                .build();

        bidder2 = User.builder()
                .nickname("bidder2")
                .providerType(ProviderType.NAVER)
                .email("bbb22@gmail.com")
                .userRole(UserRole.USER)
                .providerId("098327")
                .build();

        User seller = User.builder()
                .nickname("seller")
                .providerType(ProviderType.KAKAO)
                .email("bbb11@gmail.com")
                .userRole(UserRole.USER)
                .providerId("2222")
                .build();
        Product product1 = Product.builder()
                .category(Category.OTHER)
                .description("asd")
                .name("asd")
                .user(seller)
                .build();

        Product product2 = Product.builder()
                .category(Category.OTHER)
                .description("asd")
                .name("asd")
                .user(seller)
                .build();
        Image image1 = Image.builder()
                .product(product1)
                .cdnPath("qepifnv2")
                .build();
        Image image2 = Image.builder()
                .product(product1)
                .cdnPath("rrreww4")
                .build();
        auction1 = Auction.builder()
                .product(product1)
                .minPrice(1000)
                .status(Status.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(2))
                .winnerId(2L)
                .build();

        auction2 = Auction.builder()
                .product(product2)
                .minPrice(1000)
                .status(Status.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1))
                .winnerId(2L)
                .build();

        Bid bid1 = Bid.builder()
                .amount(1000L)
                .auction(auction1)
                .count(2)
                .bidder(bidder1)
                .build();
        Bid bid2 = Bid.builder()
                .amount(2000L)
                .auction(auction2)
                .count(3)
                .bidder(bidder1)
                .build();
        Bid bid3 = Bid.builder()
                .amount(300000L)
                .auction(auction1)
                .count(1)
                .bidder(bidder2)
                .build();
        Bid bid4 = Bid.builder()
                .amount(10000000L)
                .auction(auction2)
                .count(1)
                .bidder(bidder2)
                .build();
        userRepository.saveAll(List.of(seller, bidder1, bidder2));
        productRepository.saveAll(List.of(product1, product2));
        imageRepository.saveAll(List.of(image1, image2));
        auctionRepository.saveAll(List.of(auction1, auction2));
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4));
    }

    @Test
    @DisplayName("입찰 기록은 가격 기준으로 정렬 가능하다")
    void testFindBidHistory() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("amount"));
        Page<BiddingRecord> usersBidHistory1 = bidRepository.findUsersBidHistory(bidder1, pageable);
        Page<BiddingRecord> usersBidHistory2 = bidRepository.findUsersBidHistory(bidder2, pageable);

        // when

        // then
        assertThat(usersBidHistory1.isEmpty()).isFalse();
        assertThat(usersBidHistory1.getTotalPages()).isEqualTo(1);
        assertThat(usersBidHistory1.getNumberOfElements()).isEqualTo(2);
        assertThat(usersBidHistory1.getContent()).isSortedAccordingTo(Comparator.comparing(BiddingRecord::getBidAmount));

        assertThat(usersBidHistory2.isEmpty()).isFalse();
        assertThat(usersBidHistory2.getTotalPages()).isEqualTo(1);
        assertThat(usersBidHistory2.getNumberOfElements()).isEqualTo(2);
        assertThat(usersBidHistory2.getContent()).isSortedAccordingTo(Comparator.comparing(BiddingRecord::getBidAmount));
    }

    @Test
    @DisplayName("입찰 기록은 남은 시간 기준으로 정렬 가능하다")
    void testFindBidHistoryOrderByTimeRemaining() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("time-remaining"));
        Page<BiddingRecord> usersBidHistory1 = bidRepository.findUsersBidHistory(bidder1, pageable);
        Page<BiddingRecord> usersBidHistory2 = bidRepository.findUsersBidHistory(bidder2, pageable);
        // when

        // then
        assertThat(usersBidHistory1.isEmpty()).isFalse();
        assertThat(usersBidHistory1.getTotalPages()).isEqualTo(1);
        assertThat(usersBidHistory1.getNumberOfElements()).isEqualTo(2);
        assertThat(usersBidHistory1.getContent()).isSortedAccordingTo(
                Comparator.comparing(BiddingRecord::getTimeRemaining).reversed());

        assertThat(usersBidHistory2.isEmpty()).isFalse();
        assertThat(usersBidHistory2.getTotalPages()).isEqualTo(1);
        assertThat(usersBidHistory2.getNumberOfElements()).isEqualTo(2);
        assertThat(usersBidHistory2.getContent()).isSortedAccordingTo(
                Comparator.comparing(BiddingRecord::getTimeRemaining).reversed());

    }

    @Test
    @DisplayName("경매와 관련된 모든 입찰을 금액 내림차순으로 조회할 수 있다")
    void testFindAllBidsByAuction() {
        // given & when
        List<Bid> bidsForAuction1 = bidRepository.findAllBidsByAuction(auction1);
        List<Bid> bidsForAuction2 = bidRepository.findAllBidsByAuction(auction2);

        // then
        // Auction 1 테스트
        assertThat(bidsForAuction1).hasSize(2);
        assertThat(bidsForAuction1.get(0).getAmount()).isEqualTo(300000L);
        assertThat(bidsForAuction1.get(1).getAmount()).isEqualTo(1000L);

        // Auction 2 테스트
        assertThat(bidsForAuction2).hasSize(2);
        assertThat(bidsForAuction2.get(0).getAmount()).isEqualTo(10000000L);
        assertThat(bidsForAuction2.get(1).getAmount()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("경매에 입찰이 없을 경우 빈 리스트를 반환한다")
    void testFindAllBidsByAuctionNoBids() {
        // given
        Product product3 = Product.builder()
                .category(Category.OTHER)
                .description("product3")
                .name("product3")
                .user(userRepository.findById(1L).orElseThrow())
                .build();

        Auction auction3 = Auction.builder()
                .product(product3)
                .minPrice(1000)
                .status(Status.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(2))
                .build();

        productRepository.save(product3);
        auctionRepository.save(auction3);

        // when
        List<Bid> bidsForAuction3 = bidRepository.findAllBidsByAuction(auction3);

        // then
        assertThat(bidsForAuction3).isEmpty();
    }

}
