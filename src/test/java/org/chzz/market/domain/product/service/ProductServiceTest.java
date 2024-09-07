package org.chzz.market.domain.product.service;


import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.util.AuctionTestFactory;
import org.chzz.market.util.ProductTestFactory;
import org.chzz.market.util.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.product.entity.Product.*;
import static org.chzz.market.domain.product.entity.Product.Category.*;
import static org.chzz.market.domain.product.entity.Product.Category.ELECTRONICS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProductService productService;

    private UpdateProductRequest updateRequest;
    private Product existingProduct;
    private Product product;
    private Auction auction;
    private User user;

    private ProductTestFactory productTestFactory;
    private AuctionTestFactory auctionTestFactory;
    private UserTestFactory userTestFactory;

    @BeforeEach
    void setUp() {
        productTestFactory = new ProductTestFactory();
        auctionTestFactory = new AuctionTestFactory();
        userTestFactory = new UserTestFactory();

        user = User.builder()
                .id(1L)
                .email("test@naver.com")
                .nickname("테스트 유저")
                .build();

        product = Product.builder()
                .id(1L)
                .user(user)
                .name("사전 등록 상품")
                .description("사전 등록 상품 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .likes(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        existingProduct = builder()
                .id(1L)
                .user(user)
                .name("기존 상품")
                .description("기존 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .images(new ArrayList<>())
                .build();

        updateRequest = UpdateProductRequest.builder()
                .userId(user.getId())
                .name("수정된 상품")
                .description("수정된 설명")
                .category(HOME_APPLIANCES)
                .minPrice(20000)
                .build();

        System.setProperty("org.mockito.logging.verbosity", "all");
    }

    @Nested
    @DisplayName("사전 등록 상품 수정")
    class preRegister_Update {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 등록 상품 수정 성공 응답")
        void updateProduct_Success() {
            // given
            List<MultipartFile> images = createMockMultipartFiles();

            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            UpdateProductResponse response = productService.updateProduct(1L, updateRequest, images);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.category()).isEqualTo(HOME_APPLIANCES);
            assertThat(response.minPrice()).isEqualTo(20000);

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, times(1)).existsByProductId(1L);
            verify(imageRepository, times(1)).deleteAll(anyList());  // 이미지 삭제 확인
            verify(imageService, times(1)).uploadImages(anyList());  // 이미지 업로드 확인
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품으로 수정 시도 실패")
        void updateProduct_ProductNotFound() {
            // given
            List<MultipartFile> images = createMockMultipartFiles();
            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest, images))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다.");

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, never()).existsByProductId(anyLong());
            verify(imageService, never()).uploadImages(anyList());
            verify(imageRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("3. 이미 경매 등록된 상품 수정 시도 실패")
        void updateProduct_AlreadyInAuction() {
            // given
            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("이미 경매가 진행 중인 상품입니다.");

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, times(1)).existsByProductId(1L);
        }

        @Test
        @DisplayName("4. 이미지 없이 상품 정보만 수정 성공")
        void updateProduct_WithoutImages() {
            // given
            when(productRepository.findByIdAndUserId(anyLong(), eq(user.getId()))).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            UpdateProductResponse response = productService.updateProduct(1L, updateRequest, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.category()).isEqualTo(HOME_APPLIANCES);
            assertThat(response.minPrice()).isEqualTo(20000);

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(user.getId()));
            verify(auctionRepository, times(1)).existsByProductId(1L);
            verify(imageRepository, never()).deleteAll(anyList());
            verify(imageService, never()).uploadImages(anyList());

        }

        @Test
        @DisplayName("5. 유효하지 않은 사용자가 상품 수정 시도 실패")
        void updateProduct_InvalidUser() {
            // given
            UpdateProductRequest invalidUserRequest = UpdateProductRequest.builder()
                    .userId(999L)
                    .name("수정된 상품")
                    .description("수정된 설명")
                    .category(HOME_APPLIANCES)
                    .minPrice(20000)
                    .build();

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(1L, invalidUserRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다.");

            verify(productRepository, times(1)).findByIdAndUserId(eq(1L), eq(999L));
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 상품 삭제 성공 응답")
        void deletePreRegisteredProduct_Success() {
            // given
            when(productRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(product));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            DeleteProductResponse response = productService.deleteProduct(1L, 1L);

            // then
            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.productName()).isEqualTo("사전 등록 상품");
            assertThat(response.likeCount()).isZero();
            verify(productRepository, times(1)).delete(product);
            verify(imageService, times(1)).deleteUploadImages(any());
        }

        @Test
        @DisplayName("2. 이미 경매로 등록된 상품 삭제 시도")
        void deleteAlreadyAuctionedProduct() {
            // Given
            when(productRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(product));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("상품이 이미 경매로 등록되어 삭제할 수 없습니다.");
        }

        @Test
        @DisplayName("3. 존재하지 않는 상품 삭제 시도")
        void deleteNonExistingProduct() {
            // Given
            when(productRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(1L, 1L))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("상품을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("내가 참여한 사전경매 조회 테스트")
    class GetLikedProductListTest {
        @Test
        @DisplayName("1. 유효한 요청으로 좋아요한 사전 경매 상품 목록 조회 성공")
        void getLikedProductList_Success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

            List<ProductResponse> mockProducts = Arrays.asList(
                    new ProductResponse(1L, "Product 1", "image1.jpg", 10000, 5L, true),
                    new ProductResponse(2L, "Product 2", "image2.jpg", 20000, 10L, true)
            );

            Page<ProductResponse> mockPage = new PageImpl<>(mockProducts, pageable, mockProducts.size());

            when(productRepository.findLikedProductsByUserId(userId, pageable)).thenReturn(mockPage);

            // when
            Page<ProductResponse> result = productService.getLikedProductList(userId, pageable);

            // then
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals("Product 1", result.getContent().get(0).getName());
            assertEquals("Product 2", result.getContent().get(1).getName());
            assertTrue(result.getContent().get(0).getIsLiked());
            assertTrue(result.getContent().get(1).getIsLiked());

            verify(productRepository, times(1)).findLikedProductsByUserId(userId, pageable);
        }

        @Test
        @DisplayName("2. 좋아요 한 사전경매 상품이 없는 경우 빈 목록 반환")
        void getLikedProductList_EmptyList() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<ProductResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(productRepository.findLikedProductsByUserId(userId, pageable)).thenReturn(emptyPage);

            // when
            Page<ProductResponse> result = productService.getLikedProductList(userId, pageable);

            // then
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());

            verify(productRepository, times(1)).findLikedProductsByUserId(userId, pageable);
        }

        @Test
        @DisplayName("3. 페이지네이션 동작 확인")
        void getLikedProductList_Pagination() {
            // given
            Long userId = 1L;
            Pageable firstPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
            Pageable secondPageable = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "createdAt"));

            List<ProductResponse> allProducts = Arrays.asList(
                    new ProductResponse(1L, "Product 1", "image1.jpg", 10000, 5L, true),
                    new ProductResponse(2L, "Product 2", "image2.jpg", 20000, 3L, true)
            );

            Page<ProductResponse> firstPage = new PageImpl<>(allProducts.subList(0, 1), firstPageable, allProducts.size());
            Page<ProductResponse> secondPage = new PageImpl<>(allProducts.subList(1, 2), secondPageable, allProducts.size());

            when(productRepository.findLikedProductsByUserId(userId, firstPageable)).thenReturn(firstPage);
            when(productRepository.findLikedProductsByUserId(userId, secondPageable)).thenReturn(secondPage);

            // when
            Page<ProductResponse> firstResult = productService.getLikedProductList(userId, firstPageable);
            Page<ProductResponse> secondResult = productService.getLikedProductList(userId, secondPageable);

            // then
            assertEquals(1, firstResult.getContent().size());
            assertEquals("Product 1", firstResult.getContent().get(0).getName());
            assertEquals(1, secondResult.getContent().size());
            assertEquals("Product 2", secondResult.getContent().get(0).getName());

            verify(productRepository, times(1)).findLikedProductsByUserId(userId, firstPageable);
            verify(productRepository, times(1)).findLikedProductsByUserId(userId, secondPageable);
        }
    }

    private List<MultipartFile> createMockMultipartFiles() {
        MultipartFile mockFile1 = new MockMultipartFile(
                "testImage1.jpg", "testImage1.jpg", "image/jpeg", "test image content 1".getBytes());
        MultipartFile mockFile2 = new MockMultipartFile(
                "testImage2.jpg", "testImage2.jpg", "image/jpeg", "test image content 2".getBytes());
        return List.of(mockFile1, mockFile2);
    }
}
