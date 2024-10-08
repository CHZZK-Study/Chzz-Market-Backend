package org.chzz.market.domain.product.dto;

import static org.chzz.market.domain.product.entity.Product.Category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.ThousandMultiple;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    @Size(min = 2, message = "제목은 최소 2글자 이상이어야 합니다")
    private String productName;

    @Size(max = 1000, message = "상품 설명은 최대 1000자까지 가능합니다")
    private String description;

    private Category category;

    @ThousandMultiple
    @Min(value = 1000, message = "시작 가격은 최소 1,000원 이상, 1000의 배수이어야 합니다")
    private Integer minPrice;

    @Builder.Default
    private Map<Long,Integer> imageSequence = new HashMap<>();
}
