package org.chzz.market.domain.auction.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chzz.market.domain.product.entity.Product.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.auction.dto.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.Auction.AuctionStatus;
import org.chzz.market.domain.auction.entity.SortType;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

@DatabaseTest
@EnableJpaAuditing
@Transactional
class AuctionRepositoryImplTest {

    @Autowired
    AuctionRepository auctionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    UserRepository userRepository;

    private static User user1, user2, user3;
    private static Product product1, product2, product3, product4;
    private static Auction auction1, auction2, auction3, auction4;
    private static Image image1, image2, image3, image4;
    private static Bid bid1, bid2, bid3, bid4;

    @BeforeAll
    static void setUpOnce(@Autowired UserRepository userRepository,
                          @Autowired ProductRepository productRepository,
                          @Autowired AuctionRepository auctionRepository,
                          @Autowired ImageRepository imageRepository,
                          @Autowired BidRepository bidRepository) {
        user1 = User.builder().providerId("1234").nickname("닉네임1").email("asd@naver.com").build();
        user2 = User.builder().providerId("12345").nickname("닉네임2").email("asd1@naver.com").build();
        user3 = User.builder().providerId("123456").nickname("닉네임3").email("asd12@naver.com").build();
        userRepository.saveAll(List.of(user1, user2, user3));

        product1 = builder().user(user1).name("제품1").category(Category.FASHION_AND_CLOTHING).minPrice(1000).status(ProductStatus.PRE_REGISTERED).build();
        product2 = builder().user(user1).name("제품2").category(Category.BOOKS_AND_MEDIA).minPrice(1000).status(ProductStatus.PRE_REGISTERED).build();
        product3 = builder().user(user2).name("제품3").category(Category.FASHION_AND_CLOTHING).minPrice(1000).status(ProductStatus.PRE_REGISTERED).build();
        product4 = builder().user(user2).name("제품4").category(Category.FASHION_AND_CLOTHING).minPrice(1000).status(ProductStatus.PRE_REGISTERED).build();
        productRepository.saveAll(List.of(product1, product2, product3, product4));

        auction1 = Auction.builder().product(product1).minPrice(1000).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction2 = Auction.builder().product(product2).minPrice(2000).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction3 = Auction.builder().product(product3).minPrice(3000).status(AuctionStatus.PROCEEDING)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auction4 = Auction.builder().product(product4).minPrice(3000).status(AuctionStatus.CANCELLED)
                .endDateTime(LocalDateTime.now().plusDays(1)).build();
        auctionRepository.saveAll(List.of(auction1, auction2, auction3, auction4));

        image1 = Image.builder().product(product1).cdnPath("path/to/image1_1.jpg").build();
        image2 = Image.builder().product(product1).cdnPath("path/to/image1_2.jpg").build();
        image3 = Image.builder().product(product2).cdnPath("path/to/image2.jpg").build();
        image4 = Image.builder().product(product3).cdnPath("path/to/image3.jpg").build();
        imageRepository.saveAll(List.of(image1, image2, image3, image4));

        bid1 = Bid.builder().bidder(user2).auction(auction1).amount(2000L).build();
        bid2 = Bid.builder().bidder(user2).auction(auction2).amount(4000L).build();
        bid3 = Bid.builder().bidder(user1).auction(auction3).amount(5000L).build();
        bid4 = Bid.builder().bidder(user3).auction(auction2).amount(6000L).build();
        bidRepository.saveAll(List.of(bid1, bid2, bid3, bid4));
    }

    @Test
    @DisplayName("특정 카테고리 경매를 높은 가격순으로 조회")
    public void testFindAuctionsByCategoryExpensive() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.FASHION_AND_CLOTHING, SortType.EXPENSIVE, 1L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("제품3");
        assertThat(result.getContent().get(0).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(0).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCdnPath()).isEqualTo("path/to/image3.jpg");
        assertThat(result.getContent().get(1).getName()).isEqualTo("제품1");
        assertThat(result.getContent().get(1).getIsParticipating()).isFalse();
        assertThat(result.getContent().get(1).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(1).getCdnPath()).isEqualTo("path/to/image1_1.jpg");
    }

    @Test
    @DisplayName("특정 카테고리 경매를 인기순으로 조회")
    public void testFindAuctionsByCategoryPopularity() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.FASHION_AND_CLOTHING, SortType.POPULARITY, 2L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("제품1");
        assertThat(result.getContent().get(0).getIsParticipating()).isTrue();
        assertThat(result.getContent().get(0).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCdnPath()).isEqualTo("path/to/image1_1.jpg");
        assertThat(result.getContent().get(1).getName()).isEqualTo("제품3");
        assertThat(result.getContent().get(1).getIsParticipating()).isFalse();
        assertThat(result.getContent().get(1).getParticipantCount()).isEqualTo(1);
        assertThat(result.getContent().get(1).getCdnPath()).isEqualTo("path/to/image3.jpg");
    }

    @Test
    @DisplayName("경매가 없는 경우 조회")
    public void testFindAuctionsByCategoryNoAuctions() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<AuctionResponse> result = auctionRepository.findAuctionsByCategory(
                Category.TOYS_AND_HOBBIES, SortType.EXPENSIVE, 1L, pageable);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("경매 상세 조회 - 본인의 제품 경매인 경우")
    public void testFindAuctionDetailsById() throws Exception {
        //given
        Long auctionId = auction1.getId();
        Long userId = user1.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product1.getId());
        assertThat(result.get().getSellerId()).isEqualTo(userId);
        assertThat(result.get().getIsSeller()).isTrue();
        assertThat(result.get().getBidAmount()).isEqualTo(0);
        assertThat(result.get().getIsParticipating()).isFalse();
        assertThat(result.get().getImageList()).containsOnly(image1.getCdnPath(), image2.getCdnPath());
    }

    @Test
    @DisplayName("경매 상세 조회 - 다른 사람의 제품 경매 (참여하지 않은 경우)")
    public void testFindAuctionDetailsById_OtherUser_NotParticipating() throws Exception {
        //given
        Long auctionId = auction1.getId();
        Long userId = user3.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product1.getId());
        assertThat(result.get().getSellerId()).isEqualTo(user1.getId());
        assertThat(result.get().getIsSeller()).isFalse();
        assertThat(result.get().getBidAmount()).isEqualTo(0);
        assertThat(result.get().getIsParticipating()).isFalse();
    }

    @Test
    @DisplayName("경매 상세 조회 - 다른 사람의 제품 경매 (참여한 경우)")
    public void testFindAuctionDetailsById_OtherUser_Participating() throws Exception {
        //given
        Long auctionId = auction2.getId();
        Long userId = user3.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(product2.getId());
        assertThat(result.get().getSellerId()).isEqualTo(user1.getId());
        assertThat(result.get().getIsSeller()).isFalse();
        assertThat(result.get().getBidAmount()).isEqualTo(6000L);
        assertThat(result.get().getIsParticipating()).isTrue();
    }

    @Test
    @DisplayName("경매 상세 조회 - 취소된 경매인 경우")
    public void testFindAuctionDetailsById_CancelledAuction() throws Exception {
        //given

        Long auctionId = auction4.getId();
        Long userId = user1.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("경매 상세 조회 - 없는 경매인 경우")
    public void testFindAuctionDetailsById_NonExistentAuction() throws Exception {
        //given
        Long auctionId = 10L;
        Long userId = user1.getId();

        //when
        Optional<AuctionDetailsResponse> result = auctionRepository.findAuctionDetailsById(auctionId, userId);

        //then
        assertThat(result).isNotPresent();
    }

}
