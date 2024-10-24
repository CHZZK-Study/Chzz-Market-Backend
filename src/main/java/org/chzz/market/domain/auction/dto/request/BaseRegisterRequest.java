package org.chzz.market.domain.auction.dto.request;

import static org.chzz.market.domain.product.entity.Product.Category;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.auction.type.AuctionRegisterType;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "auctionRegisterType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "PRE_REGISTER", value = PreRegisterRequest.class),
        @JsonSubTypes.Type(name = "REGISTER", value = RegisterAuctionRequest.class)
})
public abstract class BaseRegisterRequest {

    @NotBlank
    @Size(min = 2, max = 30, message = "제목은 최소 2글자 이상 30자 이하여야 합니다")
    protected String productName;

    @Pattern(regexp = "^$|.{5,1000}$", message = "상품 설명은 최소 5자에서 최대 1000자까지 가능합니다")
    protected String description;

    @NotNull(message = "카테고리를 선택해주세요")
    protected Category category;

    @NotNull
    @ThousandMultiple
    protected Integer minPrice;

    @NotNull(message = "경매 타입을 선택해주세요")
    protected AuctionRegisterType auctionRegisterType;
}
