package org.chzz.market.domain.product.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.chzz.market.domain.product.entity.Product.Category.BOOKS_AND_MEDIA;
import static org.chzz.market.domain.product.entity.Product.Category.ELECTRONICS;
import static org.chzz.market.domain.product.entity.Product.Category.FASHION_AND_CLOTHING;
import static org.chzz.market.domain.product.entity.Product.Category.HOME_APPLIANCES;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.chzz.market.common.DatabaseTest;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@DatabaseTest
@Transactional
class ProductRepositoryCustomImplTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    LikeRepository likeRepository;

    @PersistenceContext
    EntityManager entityManager;

    private static User user1, user2, user3;
    private static Product product1, product2, product3, product4, product5;
    private static Image image1, image2, image3, image4, image5;
    private static Like like1, like2, like3;

    @BeforeAll
    static void setUpOnce(@Autowired UserRepository userRepository,
                          @Autowired ProductRepository productRepository,
                          @Autowired ImageRepository imageRepository,
                          @Autowired LikeRepository likeRepository) {
        user1 = User.builder().providerId("1234").nickname("닉네임1").email("user1@test.com").build();
        user2 = User.builder().providerId("5678").nickname("닉네임2").email("user2@test.com").build();
        user3 = User.builder().providerId("9012").nickname("닉네임3").email("user3@test.com").build();
        userRepository.saveAll(List.of(user1, user2, user3));

        product1 = Product.builder().user(user1).name("사전등록상품1").category(ELECTRONICS).minPrice(10000).build();
        ReflectionTestUtils.setField(product1, "createdAt", LocalDateTime.now().minusDays(5));
        product2 = Product.builder().user(user1).name("사전등록상품2").category(BOOKS_AND_MEDIA).minPrice(20000).build();
        ReflectionTestUtils.setField(product2, "createdAt", LocalDateTime.now().minusDays(4));
        product3 = Product.builder().user(user2).name("사전등록상품3").category(ELECTRONICS).minPrice(30000).build();
        ReflectionTestUtils.setField(product3, "createdAt", LocalDateTime.now().minusDays(3));
        product4 = Product.builder().user(user2).name("사전등록상품4").category(ELECTRONICS).minPrice(40000).build();
        ReflectionTestUtils.setField(product4, "createdAt", LocalDateTime.now().minusDays(2));
        product5 = Product.builder().user(user3).name("사전등록상품5").category(FASHION_AND_CLOTHING).minPrice(50000).build();
        ReflectionTestUtils.setField(product5, "createdAt", LocalDateTime.now().minusDays(1));
        productRepository.saveAll(List.of(product1, product2, product3, product4, product5));

        image1 = Image.builder().product(product1).cdnPath("path/to/image1.jpg").sequence(1).build();
        image2 = Image.builder().product(product2).cdnPath("path/to/image2.jpg").sequence(1).build();
        image3 = Image.builder().product(product3).cdnPath("path/to/image3.jpg").sequence(1).build();
        image4 = Image.builder().product(product4).cdnPath("path/to/image4.jpg").sequence(1).build();
        image5 = Image.builder().product(product5).cdnPath("path/to/image5.jpg").sequence(1).build();
        imageRepository.saveAll(List.of(image1, image2, image3, image4, image5));

        like1 = Like.builder().user(user2).product(product1).build();
        like2 = Like.builder().user(user3).product(product1).build();
        like3 = Like.builder().user(user1).product(product3).build();
        likeRepository.saveAll(List.of(like1, like2, like3));
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        imageRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("카테고리 별 사전 등록 상품 목록 조회 테스트")
    class FindProductsByCategoryTest {
        @Test
        @DisplayName("1. 특정 카테고리 사전 등록 상품을 높은 가격순으로 조회")
        public void testFindProductsByCategoryExpensive() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // when
            Page<ProductResponse> result = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(),
                    pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(ProductResponse::getMinPrice)
                    .isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("사전등록상품4");
            assertThat(result.getContent().get(0).getMinPrice()).isEqualTo(40000);
            assertThat(result.getContent().get(1).getProductName()).isEqualTo("사전등록상품3");
            assertThat(result.getContent().get(1).getMinPrice()).isEqualTo(30000);
            assertThat(result.getContent().get(2).getProductName()).isEqualTo("사전등록상품1");
            assertThat(result.getContent().get(2).getMinPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("2. 특정 카테고리 사전 등록 상품을 좋아요 많은 순으로 조회")
        void findProductsByCategoryOrderByMostLikes() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("like"));

            // when
            Page<ProductResponse> result = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(),
                    pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(ProductResponse::getLikeCount)
                    .isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("사전등록상품1");
            assertThat(result.getContent().get(0).getLikeCount()).isEqualTo(2);
            assertThat(result.getContent().get(1).getProductName()).isEqualTo("사전등록상품3");
            assertThat(result.getContent().get(1).getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("3. 특정 카테고리 사전 등록 상품 최신순으로 조회")
        void findProductsByCategoryOrderByNewest() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("product-newest"));

            // when
            Page<ProductResponse> result = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(),
                    pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(ProductResponse::getProductId)
                    .isSortedAccordingTo(Comparator.reverseOrder());
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("사전등록상품4");
            assertThat(result.getContent().get(1).getProductName()).isEqualTo("사전등록상품3");
            assertThat(result.getContent().get(2).getProductName()).isEqualTo("사전등록상품1");

        }

        @Test
        @DisplayName("4. 카테고리가 null 일 때 모든 사전 등록 상품 조회")
        void findProductsWithNullCategory() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // when
            Page<ProductResponse> result = productRepository.findProductsByCategory(null, user1.getId(), pageable);

            // then
            assertThat(result.getContent()).isNotNull();
            assertThat(result.getContent()).hasSizeGreaterThan(3);
            assertThat(result.getContent())
                    .extracting(ProductResponse::getMinPrice)
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }

    @Nested
    @DisplayName("사전 등록 상품 상세 정보 조회 테스트")
    class FindProductDetailsTest {

        @Test
        @DisplayName("1. 유효한 상품 ID로 상세 정보 조회")
        void findProductDetailsById() {
            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(product1.getId(),
                    user2.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getProductName()).isEqualTo("사전등록상품1");
            assertThat(result.get().getMinPrice()).isEqualTo(10000);
            assertThat(result.get().getLikeCount()).isEqualTo(2);
            assertThat(result.get().getIsLiked()).isTrue(); // user2가 좋아요 한 상품
            assertThat(result.get().getIsSeller()).isFalse();
            assertThat(result.get().getCategory()).isEqualTo(ELECTRONICS);

            assertThat(result.get().getImages()).isNotEmpty();
            assertThat(result.get().getImages()).anySatisfy(imageResponse -> {
                assertThat(imageResponse.imageUrl()).isEqualTo("path/to/image1.jpg");
            });
        }

        @Test
        @DisplayName("2. 존재하지 않는 상품 ID로 조회 시 빈 Optional 반환")
        void findProductDetailsByNonExistentId() {
            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(999L, user1.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("3. 좋아요 하지 않은 사용자가 조회 시 'isLiked' false 반환")
        void findProductDetailsWithoutLike() {
            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(product3.getId(),
                    user3.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getIsLiked()).isFalse();
        }

        @Test
        @DisplayName("4. 좋아요 없는 상품 조회 시 좋아요 수 0으로 반환")
        void findProductDetailsWithNoLikes() {
            // given
            Product productWithoutLikes = productRepository.save(
                    Product.builder().user(user1).name("좋아요 없는 상품").category(ELECTRONICS).minPrice(10000).build()
            );

            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(
                    productWithoutLikes.getId(), user1.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getLikeCount()).isZero();
        }

        @Test
        @DisplayName("5. 다른 사용자의 상품 정보도 정상적으로 조회")
        void findProductDetailsOfOtherUser() {
            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(product2.getId(),
                    user3.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSellerNickname()).isEqualTo(user1.getNickname());
        }

        @Test
        @DisplayName("6. 상품 정보에 생성 시간이 정확히 포함")
        void findProductDetailsWithCorrectCreatedAt() {
            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(product1.getId(),
                    user1.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUpdatedAt()).isNotNull();
            // 생성 시간이 현재 시간보다 과거인지 확인
            assertThat(result.get().getUpdatedAt()).isBefore(LocalDateTime.now());
        }

        @Test
        @DisplayName("7. 비안증 사용자의 요청인 경우")
        void findProductDetailsWithAnonymousUser() {
            // when
            Optional<ProductDetailsResponse> result = productRepository.findProductDetailsById(product1.getId(), null);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getProductName()).isEqualTo("사전등록상품1");
            assertThat(result.get().getMinPrice()).isEqualTo(10000);
            assertThat(result.get().getLikeCount()).isEqualTo(2);
            assertThat(result.get().getIsLiked()).isFalse(); // user2가 좋아요 한 상품
            assertThat(result.get().getIsSeller()).isFalse();
            assertThat(result.get().getCategory()).isEqualTo(ELECTRONICS);

            // 이미지 확인
            assertThat(result.get().getImages()).isNotEmpty();
            assertThat(result.get().getImages()).anySatisfy(imageResponse -> {
                assertThat(imageResponse.imageUrl()).isEqualTo("path/to/image1.jpg");
            });
        }

    }

    @Nested
    @DisplayName("나의 사전 등록 상품 목록 조회 테스트")
    class FindMyProductsTest {

        @Test
        @DisplayName("1. 유효한 사용자의 사전 등록 상품 목록 조회")
        void findMyProductsByUserId() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("product-newest"));

            // when
            Page<ProductResponse> result = productRepository.findProductsByNickname(user1.getNickname(), pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting("productName")
                    .containsExactly("사전등록상품2", "사전등록상품1");
        }

        @Test
        @DisplayName("2. 사전 등록 상품이 없는 사용자의 경우 빈 목록 반환")
        void findMyProductsByUserIdWithNoProducts() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            User newUser = userRepository.save(
                    User.builder().providerId("9999").nickname("새로운사용자").email("new@test.com").build());

            // when
            Page<ProductResponse> result = productRepository.findProductsByNickname(newUser.getNickname(), pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("3. 페이지네이션 동작 확인")
        public void testPagination() {
            // given
            Pageable firstPage = PageRequest.of(0, 2, Sort.by("expensive"));
            Pageable secondPage = PageRequest.of(1, 2, Sort.by("expensive"));

            // when
            Page<ProductResponse> firstResult = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(),
                    firstPage);
            Page<ProductResponse> secondResult = productRepository.findProductsByCategory(ELECTRONICS, user1.getId(),
                    secondPage);

            // then
            assertThat(firstResult.getContent()).hasSize(2);
            assertThat(secondResult.getContent()).hasSize(1);
            assertThat(firstResult.getContent().get(0).getProductName()).isEqualTo("사전등록상품4");
            assertThat(firstResult.getContent().get(1).getProductName()).isEqualTo("사전등록상품3");
            assertThat(secondResult.getContent().get(0).getProductName()).isEqualTo("사전등록상품1");
        }

        @Test
        @DisplayName("4. 다양한 정렬 옵션이 정상적으로 적용")
        void sortingOptionsWorkCheck() {
            // given
            Pageable newestPageable = PageRequest.of(0, 10, Sort.by("product-newest"));
            Pageable expensivePageable = PageRequest.of(0, 10, Sort.by("expensive"));

            // when
            Page<ProductResponse> newestResult = productRepository.findProductsByNickname(user1.getNickname(),
                    newestPageable);
            Page<ProductResponse> expensiveResult = productRepository.findProductsByNickname(user1.getNickname(),
                    expensivePageable);

            // then
            assertThat(newestResult.getContent()).extracting("productName")
                    .containsExactly("사전등록상품2", "사전등록상품1");
            assertThat(expensiveResult.getContent()).extracting("productName")
                    .containsExactly("사전등록상품2", "사전등록상품1");
        }

        @Test
        @DisplayName("5. 조회된 상품에 좋아요 정보 포함 확인")
        void likeInfoCheck() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<ProductResponse> result = productRepository.findProductsByNickname(user1.getNickname(), pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting("likeCount")
                    .containsExactly(2L, 0L);
        }
    }

    @Nested
    @DisplayName("사전 등록 상품 수정 테스트")
    class UpdateProducts {

        @Test
        @DisplayName("1. 상품 정보 수정 성공")
        void updateProductInfo() {
            // given
            String newName = "수정된 상품명";
            String newDescription = "수정된 설명";
            Category newCategory = HOME_APPLIANCES;
            int newMinPrice = 15000;

            // when
            Product productToUpdate = productRepository.findById(product1.getId()).orElseThrow();
            productToUpdate.update(new UpdateProductRequest(newName, newDescription, newCategory, newMinPrice, null));
            productRepository.save(productToUpdate);
            entityManager.flush();
            entityManager.clear();

            // then
            Product updatedProduct = productRepository.findById(product1.getId()).orElseThrow();
            assertThat(updatedProduct.getName()).isEqualTo(newName);
            assertThat(updatedProduct.getDescription()).isEqualTo(newDescription);
            assertThat(updatedProduct.getCategory()).isEqualTo(newCategory);
            assertThat(updatedProduct.getMinPrice()).isEqualTo(newMinPrice);
        }

        @Test
        @DisplayName("2. 이미지 추가 성공")
        void addNewImage() {
            // given
            String newImagePath = "path/to/new_image.jpg";
            Image newImage = Image.builder().product(product1).cdnPath(newImagePath).build();

            // when
            imageRepository.save(newImage);
            entityManager.flush();
            entityManager.clear();

            // then
            Product updatedProduct = productRepository.findById(product1.getId()).orElseThrow();
            assertThat(updatedProduct.getImages()).hasSize(2);
            assertThat(updatedProduct.getImages().stream().map(Image::getCdnPath)).contains(newImagePath);
        }

        @Test
        @DisplayName("3. 이미지 삭제 성공")
        void deleteImage() {
            // given
            Long imageIdToDelete = image1.getId();

            // when
            imageRepository.deleteById(imageIdToDelete);
            entityManager.flush();
            entityManager.clear();

            // then
            Product updatedProduct = productRepository.findById(product1.getId()).orElseThrow();
            assertThat(updatedProduct.getImages()).isEmpty();
        }

        @Test
        @DisplayName("4. 상품 정보 수정 및 이미지 변경 성공")
        void updateProductInfoAndChangeImages() {
            // given
            String newName = "수정된 상품명";
            String newImagePath = "path/to/new_image.jpg";
            Image newImage = Image.builder().product(product1).cdnPath(newImagePath).build();

            // when
            Product productToUpdate = productRepository.findById(product1.getId()).orElseThrow();
            productToUpdate.update(new UpdateProductRequest(newName, null, HOME_APPLIANCES, null, null));
            imageRepository.deleteById(image1.getId());
            imageRepository.save(newImage);
            productRepository.save(productToUpdate);
            entityManager.flush();
            entityManager.clear();

            // then
            Product updatedProduct = productRepository.findById(product1.getId()).orElseThrow();
            assertThat(updatedProduct.getName()).isEqualTo(newName);
            assertThat(updatedProduct.getImages()).hasSize(1);
            assertThat(updatedProduct.getImages().get(0).getCdnPath()).isEqualTo(newImagePath);
        }

        @Test
        @DisplayName("5. 존재하지 않는 상품 수정 시도")
        void updateNonExistentProduct() {
            // given
            Long nonExistentProductId = 9999L;

            // when & then
            assertThatThrownBy(() -> {
                Product nonExistentProduct = productRepository.findById(nonExistentProductId).orElseThrow();
                nonExistentProduct.update(new UpdateProductRequest("New Name", null, null, null, null));
            }).isInstanceOf(NoSuchElementException.class);
        }

    }
}
