package org.chzz.market.common.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Sort;

public class QueryDslUtil {
    /**
     * Spring Data Sort 객체를 QueryDSL OrderSpecifier 객체 배열로 변환합니다.
     *
     * @param qClass 정렬할 엔티티를 나타내는 QueryDSL 엔티티 경로 클래스입니다.
     * @param sorts  정렬 속성과 방향을 포함하는 Spring Data Sort 객체입니다.
     * @return QueryDSL OrderSpecifier 객체 배열을 반환합니다.
     */
    public static <T> OrderSpecifier<?>[] getOrderSpecifiers(EntityPathBase<T> qClass, Sort sorts) {
        return sorts.stream()
                .map(order -> makeOrderSpecifier(qClass, order))
                .toArray(OrderSpecifier[]::new);
    }

    private static <T> OrderSpecifier makeOrderSpecifier(EntityPathBase<T> qClass, Sort.Order order) {
        PathBuilder<?> pathBuilder = new PathBuilder<>(qClass.getType(), qClass.getMetadata());
        return new OrderSpecifier(convertToOrder(order), pathBuilder.get(order.getProperty()));
    }

    private static Order convertToOrder(Sort.Order order) {
        return order.isAscending() ? Order.ASC : Order.DESC;
    }
}
