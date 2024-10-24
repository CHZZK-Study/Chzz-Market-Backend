package org.chzz.market.domain.product.repository;

import static org.chzz.market.domain.product.entity.Product.Category;

import java.util.Optional;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    /**
     * 카테고리와 정렬 조건에 따라 사전 등록 상품 리스트를 조회합니다.
     * @param category 카테고리
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return         페이징된 사전 등록 상품 리스트
     */
    Page<ProductResponse> findProductsByCategory(Category category, Long userId, Pageable pageable);

    /**
     * 사용자 ID와 상품 ID에 따라 사전 등록 상품 상세 정보를 조회합니다.
     * @param productId 상품 ID
     * @param userId    사용자 ID
     * @return          상품 상세 정보
     */
    Optional<ProductDetailsResponse> findProductDetailsById(Long productId, Long userId);

    /**
     * 사용자 닉네임에 따라 사용자가 등록한 사전 등록 상품 리스트를 조회합니다.
     * @param nickname    사용자 닉네임
     * @param pageable    페이징 정보
     * @return            페이징된 사전 등록 상품 리스트
     */
    Page<ProductResponse> findProductsByNickname(String nickname, Pageable pageable);

    /**
     * 사용자 인증정보를 통해 사용자가 등록한 상품 리스트 조회
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return
     */
    Page<ProductResponse> findProductsByUserId(Long userId, Pageable pageable);
    /**
     * 사용자 ID에 따라 사용자가 참여한 사전 경매 리스트를 조회합니다.
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return         페이징된 사전 경매 리스트
     */
    Page<ProductResponse> findLikedProductsByUserId(Long userId, Pageable pageable);

    /**
     * 이미지를 fetch한 상품 조회
     * @param productId 조회할 상품 ID
     * @return
     */
    Optional<Product> findProductByIdWithImage(Long productId);
}
