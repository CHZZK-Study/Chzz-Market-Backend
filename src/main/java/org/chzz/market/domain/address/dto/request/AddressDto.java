package org.chzz.market.domain.address.dto.request;

import lombok.Builder;

@Builder
public record AddressDto(
        String roadAddress,
        String jibun,
        String zipcode,
        String detailAddress,
        boolean isDefault
) {
}