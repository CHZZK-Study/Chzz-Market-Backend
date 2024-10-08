package org.chzz.market.domain.product.controller;

import static org.chzz.market.domain.product.entity.Product.Category;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.like.service.LikeService;
import org.chzz.market.domain.product.dto.CategoryResponse;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final LikeService likeService;

    /**
     * 사전 등록 상품 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProductList(
            @RequestParam(required = false) Category category,
            @LoginUser Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductListByCategory(category, userId, pageable));
    }

    /**
     * 상품 카테고리 목록 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return ResponseEntity.ok(productService.getCategories());
    }

    /**
     * 사전 등록 상품 상세 정보 조회
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailsResponse> getProductDetails(
            @PathVariable Long productId,
            @LoginUser Long userId) {
        ProductDetailsResponse response = productService.getProductDetails(productId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 나의 사전 등록 상품 목록 조회
     */
    @GetMapping("/users/{nickname}")
    public ResponseEntity<Page<ProductResponse>> getMyProductList(
            @PathVariable String nickname,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductListByNickname(nickname, pageable));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<ProductResponse>> getRegisteredProductList(
            @LoginUser Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductListByUserId(userId, pageable));
    }

    /**
     * 내가 참여한 사전경매 조회
     */
    @GetMapping("/history")
    public ResponseEntity<Page<ProductResponse>> getLikedProductList(
            @LoginUser Long userId,
            @PageableDefault(size = 20, sort = "product-newest") Pageable pageable) {
        return ResponseEntity.ok(productService.getLikedProductList(userId, pageable));
    }

    /**
     * 사전 등록 상품 수정
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<UpdateProductResponse> updateProduct(
            @LoginUser Long userId,
            @PathVariable Long productId,
            @RequestPart("request") @Valid UpdateProductRequest request,
            @RequestPart(value = "images", required = false) Map<String, MultipartFile> images) {
        UpdateProductResponse response = productService.updateProduct(userId, productId, request, images);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 사전 등록 상품 삭제
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<DeleteProductResponse> deleteProduct(
            @PathVariable Long productId,
            @LoginUser Long userId) {
        DeleteProductResponse response = productService.deleteProduct(productId, userId);
        log.info("상품이 성공적으로 삭제되었습니다. 상품 ID: {}", productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 상품 좋아요 토글
     */
    @PostMapping("/{productId}/likes")
    public ResponseEntity<LikeResponse> toggleProductLike(
            @PathVariable Long productId,
            @LoginUser Long userId) {
        LikeResponse response = likeService.toggleLike(userId, productId);
        return ResponseEntity.ok(response);
    }
}
