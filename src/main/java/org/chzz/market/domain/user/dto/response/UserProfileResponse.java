package org.chzz.market.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.chzz.market.domain.user.entity.User;

public record UserProfileResponse(
        String nickname,
        String bio,
        String profileImageUrl,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String providerType,
        ParticipationCountsResponse participantCount,
        Long preRegisterCount,
        Long registeredAuctionCount
) {
    public static UserProfileResponse of(User user,
                                         ParticipationCountsResponse counts,
                                         Long preRegisterCount,
                                         Long registeredAuctionCount,
                                         boolean includeProviderType) {
        return new UserProfileResponse(
                user.getNickname(),
                user.getBio(),
                user.getProfileImageUrl(),
                includeProviderType ? user.getProviderType().name() : null,
                counts,
                preRegisterCount,
                registeredAuctionCount
        );
    }
}
