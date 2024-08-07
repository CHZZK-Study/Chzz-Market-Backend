package org.chzz.market.domain.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.chzz.market.domain.product.entity.Product.*;

/**
 * 경매 등록 / 사전 등록 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterProductRequest {

    // user_id : 추후 로그인 기능 도입 시 삭제
    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 2, message = "제목은 최소 2글자 이상이어야 합니다")
    private String name;

    @NotNull
    @Size(max = 1000, message = "상품 설명은 최대 1000자까지 가능합니다")
    private String description;

    @NotNull(message = "카테고리를 선택해주세요")
    private Category category;

    @NotNull
    @Min(value = 1000, message = "시작 가격은 최소 1,000원 이상이어야 합니다")
    private Integer minPrice;

    @NotNull
    @Size(min = 1, max = 5, message = "이미지는 1개 이상 5개 이하로 등록해야 합니다")
    private List<MultipartFile> images;

}
