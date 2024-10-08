package org.chzz.market.domain.product.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.product.entity.Product.Category.ELECTRONICS;
import static org.chzz.market.domain.product.entity.Product.Category.HOME_APPLIANCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProductService productService;

    private UpdateProductRequest updateRequest, updateRequest2;
    private Product existingProduct, existingProduct2;
    private Image image;
    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@naver.com")
                .nickname("테스트 유저")
                .build();

        image = Image.builder()
                .product(existingProduct)
                .cdnPath("path/to/image.jpg")
                .build();

        product = Product.builder()
                .id(1L)
                .user(user)
                .name("사전 등록 상품")
                .description("사전 등록 상품 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .images(new ArrayList<>())
                .build();

        existingProduct = Product.builder()
                .id(1L)
                .user(user)
                .name("기존 상품")
                .description("기존 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .images(new ArrayList<>())
                .build();

        existingProduct2 = Product.builder()
                .id(1L)
                .user(user)
                .name("기존 상품")
                .description("기존 설명")
                .category(ELECTRONICS)
                .minPrice(10000)
                .images(new ArrayList<>(List.of(image)))
                .build();

        updateRequest = UpdateProductRequest.builder()
                .productName("수정된 상품")
                .description("수정된 설명")
                .category(HOME_APPLIANCES)
                .minPrice(20000)
                .imageSequence(Map.of(1L, 1, 2L, 2))
                .build();

        updateRequest2 = UpdateProductRequest.builder()
                .productName("수정된 상품")
                .description("수정된 설명")
                .category(HOME_APPLIANCES)
                .minPrice(20000)
                .imageSequence(Collections.emptyMap())
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
            Map<String, MultipartFile> newImages = createMockMultipartFiles();
            List<Image> existingImages = createExistingImages();
            existingProduct.addImages(existingImages);

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));

            // when
            UpdateProductResponse response = productService.updateProduct(
                    user.getId(),
                    1L,
                    updateRequest,
                    newImages
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.productName()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.category()).isEqualTo(HOME_APPLIANCES);
            assertThat(response.minPrice()).isEqualTo(20000);
            assertEquals(2, response.imageUrls().size());

            // 기존 이미지와 변경 x
            assertThat(response.imageUrls().get(0).imageUrl()).isEqualTo("existingImage1.jpg");
            assertThat(response.imageUrls().get(0).imageId()).isEqualTo(1L);
            assertThat(response.imageUrls().get(1).imageUrl()).isEqualTo("existingImage2.jpg");
            assertThat(response.imageUrls().get(1).imageId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품으로 수정 시도 실패")
        void updateProduct_ProductNotFound() {
            // given
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(user.getId(), 1L, updateRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("3. 이미 경매 등록된 상품 수정 시도 실패")
        void updateProduct_AlreadyInAuction() {
            // given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(user.getId(), 1L, updateRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("이미 경매가 진행 중인 상품입니다.");
        }

        @Test
        @DisplayName("4. 이미지 수정 없이 상품 정보만 수정 성공")
        void updateProduct_WithoutImages() {
            // given
            when(productRepository.findById(anyLong())).thenReturn(
                    Optional.of(existingProduct2));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            UpdateProductResponse response = productService.updateProduct(
                    user.getId(),
                    1L,
                    updateRequest2,
                    new HashMap<>());

            // then
            assertThat(response).isNotNull();
            assertThat(response.productName()).isEqualTo("수정된 상품");
            assertThat(response.description()).isEqualTo("수정된 설명");
            assertThat(response.category()).isEqualTo(HOME_APPLIANCES);
            assertThat(response.minPrice()).isEqualTo(20000);
            assertEquals(1, response.imageUrls().size());
        }

        @Test
        @DisplayName("5. 유효하지 않은 사용자가 상품 수정 시도 실패")
        void updateProduct_InvalidUser() {
            // given
            UpdateProductRequest invalidUserRequest = UpdateProductRequest.builder()
                    .productName("수정된 상품")
                    .description("수정된 설명")
                    .category(HOME_APPLIANCES)
                    .minPrice(20000)
                    .build();

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(999L, 1L, invalidUserRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("6. 소유자 아닌 사용자가 상품 수정 시도 실패")
        void updateProduct_InvalidOwner() {
            // given
            UpdateProductRequest invalidUserRequest = UpdateProductRequest.builder()
                    .productName("수정된 상품")
                    .description("수정된 설명")
                    .category(HOME_APPLIANCES)
                    .minPrice(20000)
                    .build();

            when(productRepository.findById(anyLong())).thenReturn(
                    Optional.of(existingProduct));

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(2L, 1L, invalidUserRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품에 접근할 수 없습니다.");
        }

        @Test
        @DisplayName("7. 이미지 삭제 시 새 이미지 추가 테스트")
        void updateProduct_WithImageChanges() {
            // given
            Map<String, MultipartFile> newImages = createMockMultipartFiles();

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            when(imageService.uploadSequentialImages(anyMap()))
                    .thenReturn(List.of(
                            new Image(1L, "new_image1.jpg", 2, existingProduct),
                            new Image(2L, "new_image2.jpg", 1, existingProduct)
                    ));

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .productName("수정된 상품")
                    .description("수정된 설명")
                    .category(HOME_APPLIANCES)
                    .minPrice(20000)
                    .imageSequence(Collections.emptyMap())
                    .build();

            // when
            UpdateProductResponse response = productService.updateProduct(
                    user.getId(),
                    1L,
                    updateRequest,
                    newImages
            );

            // then
            assertEquals(2, response.imageUrls().size());
            verify(imageService).updateExistingImages(existingProduct, updateRequest);

            assertThat(response.imageUrls().get(0).imageUrl()).isEqualTo("new_image1.jpg");
            assertThat(response.imageUrls().get(0).imageId()).isEqualTo(1L);
            assertThat(response.imageUrls().get(1).imageUrl()).isEqualTo("new_image2.jpg");
            assertThat(response.imageUrls().get(1).imageId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("8. 권한이 없는 사용자의 상품 수정 시도 실패")
        void updateProduct_Unauthorized() {
            // given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(2L, 1L, updateRequest, null))
                    .isInstanceOf(ProductException.class)
                    .hasMessageContaining("상품에 접근할 수 없습니다.");
        }

        @Test
        @DisplayName("9. 모든 기존 이미지 삭제 후 새 이미지 한 개 추가")
        void updateProduct_EmptyImageList() {
            // Given
            Map<String, MultipartFile> newImages = Map.of("1",
                    new MockMultipartFile(
                            "newImage",
                            "new_image.jpg",
                            "image/jpeg",
                            "new image content".getBytes()
                    )
            );

            UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                    .productName("수정된 상품")
                    .description("수정된 설명")
                    .category(HOME_APPLIANCES)
                    .minPrice(20000)
                    .imageSequence(Collections.emptyMap())
                    .build();

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);
            // When
            productService.updateProduct(
                    user.getId(),
                    1L,
                    updateRequest,
                    newImages
            );

            // Then
            verify(imageService).updateExistingImages(existingProduct, updateRequest);
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

        @Test
        @DisplayName("1. 유효한 요청으로 사전 상품 삭제 성공 응답")
        void deletePreRegisteredProduct_Success() {
            // given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
            when(auctionRepository.existsByProductId(anyLong())).thenReturn(false);

            // when
            DeleteProductResponse response = productService.deleteProduct(1L, 1L);

            // then
            assertThat(response.productId()).isEqualTo(1L);
            assertThat(response.productName()).isEqualTo("사전 등록 상품");
            assertThat(response.likeCount()).isZero();
        }

        @Test
        @DisplayName("2. 이미 경매로 등록된 상품 삭제 시도")
        void deleteAlreadyAuctionedProduct() {
            // Given
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
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
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

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
            assertEquals("Product 1", result.getContent().get(0).getProductName());
            assertEquals("Product 2", result.getContent().get(1).getProductName());
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

            Page<ProductResponse> firstPage = new PageImpl<>(allProducts.subList(0, 1), firstPageable,
                    allProducts.size());
            Page<ProductResponse> secondPage = new PageImpl<>(allProducts.subList(1, 2), secondPageable,
                    allProducts.size());

            when(productRepository.findLikedProductsByUserId(userId, firstPageable)).thenReturn(firstPage);
            when(productRepository.findLikedProductsByUserId(userId, secondPageable)).thenReturn(secondPage);

            // when
            Page<ProductResponse> firstResult = productService.getLikedProductList(userId, firstPageable);
            Page<ProductResponse> secondResult = productService.getLikedProductList(userId, secondPageable);

            // then
            assertEquals(1, firstResult.getContent().size());
            assertEquals("Product 1", firstResult.getContent().get(0).getProductName());
            assertEquals(1, secondResult.getContent().size());
            assertEquals("Product 2", secondResult.getContent().get(0).getProductName());

            verify(productRepository, times(1)).findLikedProductsByUserId(userId, firstPageable);
            verify(productRepository, times(1)).findLikedProductsByUserId(userId, secondPageable);
        }
    }

    private Map<String, MultipartFile> createMockMultipartFiles() {
        MultipartFile mockFile1 = new MockMultipartFile(
                "image1",
                "image1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );

        MultipartFile mockFile2 = new MockMultipartFile(
                "image2",
                "image2.jpg",
                "image/jpeg",
                "test image content 2".getBytes()
        );

        return Map.of("1", mockFile1, "2", mockFile2);
    }

    private List<Image> createExistingImages() {
        return List.of(
                new Image(1L, "existingImage1.jpg", 1, existingProduct),
                new Image(2L, "existingImage2.jpg", 2, existingProduct)
        );
    }
}
