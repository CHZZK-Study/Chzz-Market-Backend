package org.chzz.market.domain.notification.service;

import static org.chzz.market.domain.notification.error.NotificationErrorCode.REDIS_MESSAGE_SEND_FAILURE;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.notification.error.NotificationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper objectMapper;

    public void publish(NotificationMessage notificationMessage) {
        try {
            redisTemplate.convertAndSend(topic.getTopic(), objectMapper.writeValueAsString(notificationMessage));
        } catch (Exception e) {
            throw new NotificationException(REDIS_MESSAGE_SEND_FAILURE);
        }
    }
}
