package org.chzz.market.domain.product.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.error.ProductErrorCode;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.user.entity.User;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Builder
@Table(indexes = {
        @Index(name = "idx_product_id_name", columnList = "product_id, productName")
})
@DynamicUpdate
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
    // @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,}$",message = "invalid type of bidderNickname")
    private String name;

    @Column(length = 1000)
    //TODO 2024 07 18 13:35:30 : custom validate
    private String description;

    @Column
    private Integer minPrice;

    @Column(nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<Image> images = new ArrayList<>();

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

    // 좋아요 수 계산 메서드
    public int getLikeCount() {
        return likes.size();
    }

    // 좋아요 추가 메서드
    public void addLike(Like like) {
        likes.add(like);
    }

    // 좋아요 제거 메서드
    public void removeLike(Like like) {
        likes.remove(like);
    }


    public void update(UpdateProductRequest modifiedProduct) {
        this.name = modifiedProduct.getProductName();
        this.description = modifiedProduct.getDescription();
        this.category = modifiedProduct.getCategory();
        this.minPrice = modifiedProduct.getMinPrice();
    }

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }


    public void addImages(List<Image> images) {
        this.images.addAll(images);
    }


    public Image getFirstImage() {
        return images.stream()
                .filter(image -> image.getSequence() == 1)
                .findFirst()
                .orElseThrow(() -> new ProductException(ProductErrorCode.IMAGE_NOT_FOUND));
    }

    public List<Long> getLikeUserIds() {
        return likes.stream()
                .map(like -> like.getUser().getId())
                .distinct()
                .toList();
    }

}
