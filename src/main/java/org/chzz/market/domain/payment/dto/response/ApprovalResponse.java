package org.chzz.market.domain.payment.dto.response;

import jakarta.validation.constraints.Email;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record ApprovalResponse(
        String payType,
        Long amount,
        String orderId,
        String orderName,
        @Email
        String customerEmail,
        String customerName,
        String successUrl,
        String failUrl,
        ZonedDateTime createDate,
        String status
) {
    public static ApprovalResponse of(TossPaymentResponse tossPaymentResponse) {
        return ApprovalResponse.builder()
                .payType(tossPaymentResponse.getType())
                .amount(tossPaymentResponse.getTotalAmount())
                .orderId(tossPaymentResponse.getOrderId())
                .orderName(tossPaymentResponse.getOrderName())
                .createDate(tossPaymentResponse.getApprovedAt())
                .status(tossPaymentResponse.getStatus().name())
                .build();
    }
}
