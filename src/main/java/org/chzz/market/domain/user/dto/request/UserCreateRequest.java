package org.chzz.market.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserCreateRequest {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 15, message = "닉네임은 15자 이내여야 합니다.")
    private String nickname;

    @Size(max = 500, message = "자기소개는 500자 이내여야 합니다.")
    private String bio;
}
