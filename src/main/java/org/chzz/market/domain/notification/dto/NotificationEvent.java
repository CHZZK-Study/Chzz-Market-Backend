package org.chzz.market.domain.notification.dto;

import java.util.Map;
import org.chzz.market.domain.user.entity.User;

public record NotificationEvent(NotificationMessage notificationMessage, Map<Long, User> userMap) {
}
