package org.chzz.market.domain.notification.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {
    /**
     * 주어진 사용자 ID에 해당하는 SSE 이미터 목록을 찾습니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 SSE 이미터 목록이 포함된 Optional 객체. 이미터가 없는 경우 비어있는 Optional을 반환합니다.
     */
    Optional<List<SseEmitter>> findByUserId(Long userId);

    /**
     * 모든 사용자에 대한 SSE 이미터를 반환합니다.
     *
     * @return 사용자 ID를 키로 하고, 해당 사용자와 연결된 SSE 이미터 목록을 값으로 하는 맵입니다.
     */
    Map<Long, List<SseEmitter>> findAllEmitters();

    /**
     * 주어진 사용자 ID에 SSE 이미터를 저장합니다.
     *
     * @param userId  사용자 ID
     * @param emitter 저장할 SSE 이미터
     */
    void save(Long userId, SseEmitter emitter);

    /**
     * 주어진 사용자 ID에 연결된 SSE 이미터를 삭제합니다.
     *
     * @param userId  사용자 ID
     * @param emitter 삭제할 SSE 이미터
     */
    void deleteEmitter(Long userId, SseEmitter emitter);

}
