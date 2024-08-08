package org.chzz.market.domain.notification.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {
    void save(Long userId, SseEmitter emitter);

    void deleteById(Long userId);

    Optional<SseEmitter> findById(Long userId);

    List<Long> findAllId();
}
