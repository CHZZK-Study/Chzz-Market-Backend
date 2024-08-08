package org.chzz.market.domain.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationEvent;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.repository.EmitterRepositoryImpl;
import org.chzz.market.domain.notification.repository.NotificationRepository;
import org.chzz.market.domain.user.entity.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmitterRepositoryImpl emitterRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자 ID로 SSE 연결을 생성하고 구독을 처리합니다.
     *
     * @param userId 구독할 사용자 ID
     * @return SseEmitter 객체
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterRepository.save(userId, emitter);
        setupEmitterCallbacks(userId, emitter);
        sendInitialConnectionEvent(userId, emitter);
        return emitter;
    }

    /**
     * 알림 메시지를 처리하고 알림을 저장한 후 이벤트를 발행합니다.
     *
     * @param notificationMessage 알림 메시지 데이터
     * @param userMap             사용자 ID와 사용자 객체의 매핑
     */
    @Transactional
    public void processNotification(NotificationMessage notificationMessage, Map<Long, User> userMap) {
        List<Notification> notifications = createNotifications(notificationMessage, userMap);
        notificationRepository.saveAll(notifications);
        eventPublisher.publishEvent(new NotificationEvent(notificationMessage, userMap));
    }

    /**
     * SseEmitter의 콜백을 설정합니다.
     *
     * @param userId  사용자 ID
     * @param emitter 설정할 SseEmitter
     */
    private void setupEmitterCallbacks(Long userId, SseEmitter emitter) {
        emitter.onCompletion(() -> {
            emitterRepository.deleteById(userId);
            log.info("SSE connection completed for user {}", userId);
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            log.info("SSE connection timed out for user {}", userId);
        });
        emitter.onError((e) -> {
            emitter.complete();
            log.error("SSE connection error for user {}", userId);
        });
    }

    /**
     * 초기 연결 시 더미 이벤트를 전송하여 503 에러를 방지합니다.
     *
     * @param userId  사용자 ID
     * @param emitter 이벤트를 전송할 SseEmitter
     */
    private void sendInitialConnectionEvent(Long userId, SseEmitter emitter) {
        try {
            log.info("User {} subscribed to notifications with initial connection", userId);
            emitter.send(SseEmitter.event()
                    .id(userId + "_" + Instant.now().toEpochMilli())
                    .name("init")
                    .data("Connection Established"));
        } catch (Exception e) {
            log.error("Error sending initial connection event to user {}", userId);
        }
    }

    /**
     * 알림 객체 목록을 생성합니다.
     *
     * @param notificationMessage 알림 메시지 데이터
     * @param userMap             사용자 ID와 사용자 객체의 매핑
     * @return 생성된 알림 객체 목록
     */
    private List<Notification> createNotifications(NotificationMessage notificationMessage,
                                                   Map<Long, User> userMap) {
        return notificationMessage.getUserIds().stream()
                .map(userId -> createNotification(notificationMessage, userMap, userId))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 알림 객체를 생성합니다.
     *
     * @param notificationMessage 알림 메시지 데이터
     * @param userMap             사용자 ID와 사용자 객체의 매핑
     * @param userId              사용자 ID
     * @return 생성된 알림 객체, 사용자 존재 시
     */
    private Notification createNotification(NotificationMessage notificationMessage, Map<Long, User> userMap,
                                            Long userId) {
        User user = userMap.get(userId);
        return user != null ? Notification.builder()
                .message(notificationMessage.getMessage())
                .user(user)
                .type(notificationMessage.getType())
                .build() : null;
    }
}
