package org.chzz.market.domain.product.dto.response;

import lombok.Getter;
import static org.chzz.market.domain.product.entity.Product.*;

/**
 * 경매 등록 / 사전 등록 DTO
 */
public record RegisterProductResponse(Long productId, ProductStatus status, String message) {
}
