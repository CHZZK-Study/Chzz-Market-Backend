package org.chzz.market.domain.product.service;

import jakarta.persistence.EntityManager;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.product.dto.request.RegisterProductRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.response.RegisterProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductErrorCode;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.product.entity.Product.*;

@Transactional
@SpringBootTest
@Import(AWSConfig.class)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private ImageService imageService;

    private RegisterProductRequest validRequest;

    @BeforeEach
    void setup() {
        User testUser = User.builder()
                .providerId("test1234")
                .nickname("테스터")
                .email("test@example.com")
                .build();
        userRepository.save(testUser);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "test2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 2".getBytes()
        );

        MockMultipartFile image3 = new MockMultipartFile(
                "images",
                "test3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 3".getBytes()
        );

        validRequest = RegisterProductRequest.builder()
                .userId(testUser.getId())
                .name("테스트 상품")
                .description("테스트 설명")
                .category(Category.ELECTRONICS)
                .minPrice(10000)
                .images(List.of(image1, image2, image3))
                .build();
    }

    @AfterEach
    void tearDown() {
        auctionRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("경매 상품 등록 테스트")
    class RegisterAuctionProductTest {

        @Test
        @DisplayName("유효한 요청으로 경매 상품 등록 성공")
        void registerAuctionProduct_Success() {

            // when : 경매 상품 등록 서비스 실행
            RegisterProductResponse response = productService.registerAuctionProduct(validRequest);

            // Then : 경매 상품 등록 결과 검증
            assertThat(response).isNotNull();
            assertThat(response.productId()).isNotNull();
            assertThat(response.status()).isEqualTo(ProductStatus.IN_AUCTION);

            Product savedProduct = productRepository.findById(response.productId()).orElseThrow();
            assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
            assertThat(savedProduct.getDescription()).isEqualTo("테스트 설명");
            assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.IN_AUCTION);

            Auction savedAuction = auctionRepository.findByProduct(savedProduct).orElseThrow();
            assertThat(savedAuction.getMinPrice()).isEqualTo(10000);

        }

        @Test
        @DisplayName("존재하지 않는 사용자로 경매 상품 등록 실패")
        void registerAuctionProduct_InvalidUser() {
            RegisterProductRequest invalidUserRequest = RegisterProductRequest.builder()
                    .userId(9999L)
                    .name(validRequest.getName())
                    .description(validRequest.getDescription())
                    .category(validRequest.getCategory())
                    .minPrice(validRequest.getMinPrice())
                    .images(validRequest.getImages())
                    .build();

            assertThatThrownBy(() -> productService.registerAuctionProduct(invalidUserRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("사전 등록 테스트")
    class PreRegisterProductTest {
        @Test
        @DisplayName("유효한 요청으로 사전 등록 성공")
        void preRegisterProduct_Success() {
            RegisterProductResponse response = productService.preRegisterProduct(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.productId()).isNotNull();
            assertThat(response.status()).isEqualTo(ProductStatus.PRE_REGISTERED);

            Product savedProduct = productRepository.findById(response.productId()).orElseThrow();
            assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
            assertThat(savedProduct.getDescription()).isEqualTo("테스트 설명");
            assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.PRE_REGISTERED);

            Auction savedAuction = auctionRepository.findByProduct(savedProduct).orElseThrow();
            assertThat(savedAuction.getMinPrice()).isEqualTo(10000);
            assertThat(savedAuction.getStatus()).isEqualTo(Auction.AuctionStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 사전 등록 실패")
        void preRegisterProduct_InvalidUser() {
            RegisterProductRequest invalidUserRequest = RegisterProductRequest.builder()
                    .userId(9999L)
                    .name(validRequest.getName())
                    .description(validRequest.getDescription())
                    .category(validRequest.getCategory())
                    .minPrice(validRequest.getMinPrice())
                    .images(validRequest.getImages())
                    .build();

            assertThatThrownBy(() -> productService.preRegisterProduct(invalidUserRequest))
                    .isInstanceOf(ProductException.class)
                    .satisfies(exception -> {
                        ProductException productException = (ProductException) exception;
                        assertThat(productException.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_REGISTER_FAILED);
                        assertThat(productException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(productException.getErrorCode().getMessage()).isEqualTo("상품 등록에 실패했습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("사전 등록에서 경매 등록 전환 테스트")
    class ConvertToAuctionTest {

        private Long preRegisteredProductId;

        @BeforeEach
        void setup() {
            RegisterProductResponse response = productService.preRegisterProduct(validRequest);
            preRegisteredProductId = response.productId();
        }

        @Test
        @DisplayName("사전 등록 상품을 경매 등록으로 성공적으로 전환")
        void convertToAuction_Success() {
            RegisterProductResponse response = productService.convertToAuction(preRegisteredProductId);

            assertThat(response).isNotNull();
            assertThat(response.productId()).isEqualTo(preRegisteredProductId);
            assertThat(response.status()).isEqualTo(ProductStatus.IN_AUCTION);

            Product convertedProduct = productRepository.findById(preRegisteredProductId).orElseThrow();
            assertThat(convertedProduct.getStatus()).isEqualTo(ProductStatus.IN_AUCTION);

            Auction convertedAuction = auctionRepository.findByProduct(convertedProduct).orElseThrow();
            assertThat(convertedAuction.getStatus()).isEqualTo(Auction.AuctionStatus.PROCEEDING);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 전환 시도 시 실패")
        void convertToAuction_NonExistentProduct() {
            assertThatThrownBy(() -> productService.convertToAuction(9999L))
                    .isInstanceOf(ProductException.class)
                    .satisfies(exception -> {
                        ProductException productException = (ProductException) exception;
                        assertThat(productException.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
                        assertThat(productException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(productException.getErrorCode().getMessage()).isEqualTo("상품을 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("이미 경매 중인 상품을 전환 시도 시 실패")
        void convertToAuction_AlreadyInAuction() {
            productService.convertToAuction(preRegisteredProductId);

            assertThatThrownBy(() -> productService.convertToAuction(preRegisteredProductId))
                    .isInstanceOf(ProductException.class)
                    .satisfies(exception -> {
                        ProductException productException = (ProductException) exception;
                        assertThat(productException.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRODUCT_STATE);
                        assertThat(productException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(productException.getErrorCode().getMessage()).isEqualTo("상품 상태가 유효하지 않습니다.");
                    });
        }

        @Test
        @DisplayName("이미 판매 완료된 상품을 경매로 전환 시도 시 실패")
        void convertToAuction_AlreadySold() {
            // 먼저 상품을 사전 등록
            RegisterProductResponse response = productService.preRegisterProduct(validRequest);
            Long productId = response.productId();

            // 상품 상태를 판매 완료로 변경
            Product product = productRepository.findById(productId).orElseThrow();
            product.convertToAuction(); // 먼저 경매 상태로 변경
            product.convertToSold();   // 그 다음 판매 완료로 변경
            productRepository.save(product);

            // 판매 완료된 상품을 경매로 전환 시도
            assertThatThrownBy(() -> productService.convertToAuction(productId))
                    .isInstanceOf(ProductException.class)
                    .satisfies(exception -> {
                        ProductException productException = (ProductException) exception;
                        assertThat(productException.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRODUCT_STATE);
                        assertThat(productException.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(productException.getErrorCode().getMessage()).isEqualTo("상품 상태가 유효하지 않습니다.");
                    });
        }

        @Test
        @Transactional
        @DisplayName("경매 전환 시 모든 변경사항이 ROLLBACK 되는지 확인")
        void convertToAuction_EnsureTranscationRollback() {
            // 상품 사전 등록
            RegisterProductResponse response = productService.preRegisterProduct(validRequest);
            Long productId = response.productId();

            // 의도적으로 실패 유발 경매 전환 시도
            assertThatThrownBy(() -> productService.convertToAuctionWithPossibleFailure(productId, true))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("테스트를 위한 실패 옵션");

            entityManager.clear();

            // 상품 상태 원래대로 유지되었는지 확인
            Product product = productRepository.findById(productId).orElseThrow();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.PRE_REGISTERED);

            // 경매 상태 원래대로 유지되었는지 확인
            Auction auction = auctionRepository.findByProduct(product).orElseThrow();
            assertThat(auction.getStatus()).isEqualTo(Auction.AuctionStatus.PENDING);
        }

        @Test
        @DisplayName("경매 전환 후 모든 관련 엔티티의 상태가 일관되게 변경되었는지 확인")
        void convertToAuction_EnsureDataConsistency() {
            // 상품 사전 등록
            RegisterProductResponse response = productService.preRegisterProduct(validRequest);
            Long productId = response.productId();

            // 경매로 전환
            productService.convertToAuction(productId);

            // 상품 상태 확인
            Product product = productRepository.findById(productId).orElseThrow();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.IN_AUCTION);

            // 경매 상태 확인
            Auction auction = auctionRepository.findByProduct(product).orElseThrow();
            assertThat(auction.getStatus()).isEqualTo(Auction.AuctionStatus.PROCEEDING);
        }
    }
}
