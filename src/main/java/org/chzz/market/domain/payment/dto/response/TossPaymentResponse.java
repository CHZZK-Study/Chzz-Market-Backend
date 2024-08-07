package org.chzz.market.domain.payment.dto.response;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.chzz.market.domain.payment.entity.Payment.PaymentMethod;
import org.chzz.market.domain.payment.entity.Status;
import org.springframework.lang.Nullable;

@Data
public class TossPaymentResponse {
    private String version;
    private String paymentKey;
    private String type;
    private String orderId;
    private String orderName;
    private String mId;
    private String currency;
    private PaymentMethod method;
    private Long totalAmount;
    private Integer balanceAmount;
    private Status status;
    private Boolean useEscrow;
    @Nullable
    private String lastTransactionKey;
    private Integer suppliedAmount;
    private Integer vat;
    private Boolean cultureExpense;
    private Integer taxFreeAmount;
    private Integer taxExemptionAmount;
    private Boolean isPartialCancelable;
    private ZonedDateTime requestedAt;
    private ZonedDateTime approvedAt;
    @Nullable
    private Card card;
    private VirtualAccount virtualAccount;
    @Nullable
    private List<Cancel> cancels;

    public record Cancel(Integer cancelAmount,
                         String cancelReason,
                         Integer taxFreeAmount,
                         Integer taxExemptionAmount,
                         Integer refundableAmount,
                         Integer easyPayDiscountAmount,
                         ZonedDateTime canceledAt,
                         String transactionKey,
                         @Nullable
                         String receiptKey,
                         String cancelStatus,
                         @Nullable
                         String cancelRequestId) {
    }

    public record Card(Integer amount,
                       String issuerCode,
                       String acquirerCode,
                       String number,
                       Integer installmentPlanMonths,
                       String approveNo,
                       Boolean useCardPoInteger,
                       String cardType,
                       String ownerType,
                       String acquireStatus,
                       Boolean isInterestFree,
                       String interestPayer) {
    }

    public record VirtualAccount(String accountType,
                                 String accountNumber,
                                 String bankCode,
                                 String customerName,
                                 ZonedDateTime dueDate,
                                 String refundStatus,
                                 Boolean expired,
                                 String settlementStatus,
                                 RefundReceiveAccount refundReceiveAccount) {
    }

    public record RefundReceiveAccount(String bankCode,
                                       String accountNumber,
                                       String holderName) {
    }


    @Getter
    @AllArgsConstructor
    enum Type {
        NORMAL,
        BILLING,
        BRANDPAY;
    }
}
