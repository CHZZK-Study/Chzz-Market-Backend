package org.chzz.market.domain.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import org.chzz.market.domain.image.dto.ImageResponse;
import org.chzz.market.domain.product.entity.Product.Category;

/**
 * 사전 등록 상품 상세 조회 DTO
 */
@Getter
public class ProductDetailsResponse {
    private final Long productId;
    private final String productName;
    private final String sellerNickname;
    private final String sellerProfileImageUrl;
    private final Integer minPrice;
    private final LocalDateTime updatedAt;
    private final String description;
    private final Long likeCount;
    private final Boolean isLiked;
    private final Boolean isSeller;
    private final Category category;
    private List<ImageResponse> images;

    @QueryProjection
    public ProductDetailsResponse(Long productId, String productName, String sellerNickname,
                                  String sellerProfileImageUrl,
                                  Integer minPrice, LocalDateTime updatedAt, String description,
                                  Long likeCount, Boolean isLiked, Boolean isSeller, Category category) {
        this.productId = productId;
        this.productName = productName;
        this.sellerNickname = sellerNickname;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
        this.minPrice = minPrice;
        this.updatedAt = updatedAt;
        this.description = description;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.isSeller = isSeller;
        this.category = category;
    }

    public void addImageList(List<ImageResponse> images) {
        this.images = images;
    }
}
