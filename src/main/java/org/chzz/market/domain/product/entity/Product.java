package org.chzz.market.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.error.ProductErrorCode;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    // @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,}$",message = "invalid type of nickname")
    private String name;

    @Column(length = 1000)
    //TODO 2024 07 18 13:35:30 : custom validate
    private String description;

    @Column(nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private Category category;

    // 사전 등록에도 경매 시작가는 포함
    @Column(nullable = false)
    @ThousandMultiple
    private Integer minPrice;

    // 상품도 상태 관리가 필요함
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "product")
    private List<Like> likes = new ArrayList<>();

    @Getter
    @AllArgsConstructor
    public enum Category {
        ELECTRONICS("전자기기"),
        HOME_APPLIANCES("가전제품"),
        FASHION_AND_CLOTHING("패션 및 의류"),
        FURNITURE_AND_INTERIOR("가구 및 인테리어"),
        BOOKS_AND_MEDIA("도서 및 미디어"),
        SPORTS_AND_LEISURE("스포츠 및 레저"),
        TOYS_AND_HOBBIES("장난감 및 취미"),
        OTHER("기타");

        private final String displayName;
    }

    @Getter
    @AllArgsConstructor
    public enum ProductStatus {
        PRE_REGISTERED("사전 등록"),
        IN_AUCTION("경매 등록"),
        SOLD("판매 완료"),
        CANCELLED("취소 됨");

        private final String description;
    }

    // 사전 등록 -> 경매 등록 상태 변경
    public void convertToAuction() {
        if (this.status != ProductStatus.PRE_REGISTERED) {
            throw new ProductException(ProductErrorCode.INVALID_PRODUCT_STATE);
        }
        this.status = ProductStatus.IN_AUCTION;
    }

    // 경매 등록 -> 판매 됨 상태 변경
    public void convertToSold() {
        if (this.status != ProductStatus.IN_AUCTION) {
            throw new IllegalStateException("경매 중인 제품만 판매됨으로 표시할 수 있습니다.");
        }
        this.status = ProductStatus.SOLD;
    }

}
