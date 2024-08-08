package org.chzz.market.domain.notification.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationEvent;
import org.chzz.market.domain.notification.repository.EmitterRepositoryImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final EmitterRepositoryImpl emitterRepository;

    @TransactionalEventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Handling NotificationEvent - Message: {}, UserIds: {}", event.notificationMessage().getMessage(),
                event.notificationMessage().getUserIds());
        event.notificationMessage().getUserIds()
                .forEach(userId -> sendRealTimeNotification(event.notificationMessage().getMessage(), userId));
    }

    /**
     * 실시간으로 SSE를 통해 사용자에게 알림을 전송합니다.
     *
     * @param message 전송할 알림 메시지
     * @param userId  사용자 ID
     */
    private void sendRealTimeNotification(String message, Long userId) {
        Optional<SseEmitter> findEmitter = emitterRepository.findById(userId);
        findEmitter.ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(userId + "_" + Instant.now().toEpochMilli())
                        .name("notification")
                        .data(message));
            } catch (IOException e) {
                log.error("Error sending SSE event to user {}", userId);
            }
        });
    }
}
