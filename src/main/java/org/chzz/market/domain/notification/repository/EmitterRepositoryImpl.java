package org.chzz.market.domain.notification.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@Slf4j
public class EmitterRepositoryImpl implements EmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public void save(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        log.info("Saved SSE Emitter for user {}", userId);
    }

    @Override
    public void deleteById(Long userId) {
        emitters.remove(userId);
        log.info("Deleted SSE Emitter for user {}", userId);
    }

    @Override
    public Optional<SseEmitter> findById(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    @Override
    public List<Long> findAllId() {
        return emitters.keySet().stream().toList();
    }
}
