package org.chzz.market.domain.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.user.entity.User;

@Builder
public record DeliveryRequest(
        @NotBlank(message = "도로명 주소는 필수입니다.")
        String roadAddress,

        String jibun,

        @NotBlank(message = "우편번호는 필수 입력 사항입니다.")
        @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
        String zipcode,

        @NotBlank(message = "상세 주소는 필수 입력 사항입니다.")
        String detailAddress,

        @NotBlank(message = "수취인 이름은 필수 입력 사항입니다.")
        String recipientName,

        @NotBlank(message = "전화번호는 필수 입력 사항입니다.")
        @Pattern(regexp = "^(01[016789]-?\\d{3,4}-?\\d{4})$", message = "전화번호 형식이 올바르지 않습니다.")
        String phoneNumber,

        boolean isDefault
) {
    public static Address toEntity(User user, DeliveryRequest dto) {
        return Address.builder()
                .user(user)
                .roadAddress(dto.roadAddress())
                .jibun(dto.jibun())
                .zipcode(dto.zipcode())
                .detailAddress(dto.detailAddress())
                .recipientName(dto.recipientName())
                .phoneNumber(dto.phoneNumber())
                .isDefault(dto.isDefault())
                .build();
    }
}