package org.chzz.market.domain.user.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.bank_account.entity.BankAccount;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.payment.entity.Payment;
import org.chzz.market.domain.product.entity.Product;

@Getter
@Entity
@Builder
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String providerId;

    @Column(length = 25)
    private String nickname;

    @Column
    private String description;

    @Column(nullable = false)
    @Email(message = "invalid type of email")
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String region;

    private String link;

    // 구현 방식에 따라 권한 설정이 달라질 수 있어 임의로 열거체 선언 하였습니다
    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Product> products = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Payment> payments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<BankAccount> bankAccounts = new ArrayList<>();

    public void updateProfile(String nickname, String description, String region, String link) {
        this.nickname = Objects.requireNonNull(nickname, "닉네임은 필수 입력 항목입니다.");
        this.description = description;
        this.region = region;
        this.link = link;
    }

    public enum UserRole {
        USER, ADMIN, SELLER // Test 실행을 위해 임시 추가
    }

    public enum ProviderType {
        LOCAL, NAVER, KAKAO // Test 실행을 위해 임시 추가
    }
}
