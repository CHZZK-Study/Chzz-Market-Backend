package org.chzz.market.domain.address.dto.request;

import lombok.Builder;

@Builder
public record DeliveryDto(
        AddressDto addressDto,
        String recipientName,
        String phoneNumber,
        String deliveryMemo
) {
}
