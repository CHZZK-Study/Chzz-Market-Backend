package org.chzz.market.domain.notification.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@Slf4j
public class EmitterRepositoryImpl implements EmitterRepository {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public Optional<List<SseEmitter>> findByUserId(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    @Override
    public Map<Long, List<SseEmitter>> findAllEmitters() {
        return Collections.unmodifiableMap(emitters);
    }

    @Override
    public void save(Long userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("[SSE] 연결 저장 UserId: {}", userId);
    }

    @Override
    public void deleteEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            log.info("[SSE] 연결 삭제 UserId: {}", userId);

            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

}
