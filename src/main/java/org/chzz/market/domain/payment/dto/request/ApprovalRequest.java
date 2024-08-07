package org.chzz.market.domain.payment.dto.request;

public record ApprovalRequest(String orderId,
                              String paymentKey,
                              Long amount,
                              Long auctionId) {
}
