package org.chzz.market.domain.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.payment.dto.response.TossPaymentResponse;
import org.chzz.market.domain.payment.error.PaymentErrorCode;
import org.chzz.market.domain.payment.error.PaymentException;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(nullable = false)//TODO 2024 07 18 14:07:01 : custom validation
    private Long amount;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String paymentKey;

    @PrePersist
    protected void onPrePersist() {
        if (this.status == null) {
            this.status = Status.READY;
        }
    }

    private Payment(User payer, Auction auction, Long amount, PaymentMethod method, Status status, String orderId,
                    String paymentKey) {
        this.payer = payer;
        this.auction = auction;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
    }

    public static Payment of(TossPaymentResponse tossPaymentResponse, Auction auction) {
        return new Payment(
                auction.getProduct().getUser(),
                auction,
                tossPaymentResponse.getTotalAmount(),
                tossPaymentResponse.getMethod(),
                tossPaymentResponse.getStatus(),
                tossPaymentResponse.getOrderId(),
                tossPaymentResponse.getPaymentKey());
    }

    @AllArgsConstructor
    public enum PaymentMethod {
        CARD("카드"),
        VIRTUAL_ACCOUNT("가상계좌"),
        EASY_PAYMENT("간편결제"),
        MOBILE("휴대폰"),
        ACCOUNT_TRANSFER("계좌이체"),
        CULTURE_GIFT_CARD("문화상품권"),
        BOOK_CULTURE_GIFT_CARD("도서문화상품권"),
        GAME_CULTURE_GIFT_CARD("게임문화상품권"),
        CASH("테스트용");

        private final String description;

        @JsonValue
        public String getDescription() {
            return this.description;
        }

        @JsonCreator
        public static PaymentMethod fromDescription(String description) {
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.getDescription().equals(description)) {
                    return method;
                }
            }
            throw new PaymentException(PaymentErrorCode.INVALID_METHOD);
        }
    }
}
