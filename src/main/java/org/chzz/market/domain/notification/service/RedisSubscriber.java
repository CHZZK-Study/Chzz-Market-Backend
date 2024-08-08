package org.chzz.market.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RedisSubscriber {

    private final NotificationService notificationService;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Redis에서 발행된 메시지를 수신하고 사용자에게 알림을 보냅니다.
     *
     * @param message 수신한 메시지
     */
    public void onMessage(String message) {
        executor.execute(() -> {
            try {
                NotificationMessage notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
                List<User> users = userRepository.findAllById(notificationMessage.getUserIds());
                Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
                notificationService.processNotification(notificationMessage, userMap);
            } catch (Exception e) {
                log.error("Error handling message");
            }
        });
    }

}
