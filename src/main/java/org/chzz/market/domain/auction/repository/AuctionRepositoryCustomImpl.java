package org.chzz.market.domain.auction.repository;

import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilder;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.auction.repository.AuctionRepositoryCustomImpl.AuctionOrder.NEWEST;
import static org.chzz.market.domain.auction.repository.AuctionRepositoryCustomImpl.AuctionOrder.POPULARITY;
import static org.chzz.market.domain.auction.type.AuctionStatus.ENDED;
import static org.chzz.market.domain.auction.type.AuctionStatus.PROCEEDING;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.CANCELLED;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.order.entity.QOrder.order;
import static org.chzz.market.domain.product.entity.QProduct.product;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.QAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QLostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QSimpleAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QUserAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QUserEndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QWonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.QWonAuctionResponse;
import org.chzz.market.domain.auction.dto.response.SimpleAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserEndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.bid.entity.QBid;
import org.chzz.market.domain.image.dto.ImageResponse;
import org.chzz.market.domain.image.dto.QImageResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class AuctionRepositoryCustomImpl implements AuctionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 카테고리와 정렬 조건에 따라 경매 리스트를 조회합니다.
     *
     * @param category 카테고리
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 경매 응답 리스트
     */
    @Override
    public Page<AuctionResponse> findAuctionsByCategory(Category category, Long userId,
                                                        Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.product, product)
                .where(auction.product.category.eq(category).and(auction.status.eq(PROCEEDING)));

        List<AuctionResponse> content = baseQuery
                .select(new QAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        bid.countDistinct(),
                        isParticipating(userId)
                ))
                .leftJoin(bid).on(bid.auction.id.eq(auction.id).and(bid.status.eq(ACTIVE)))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .groupBy(auction.id, product.name, image.cdnPath, auction.createdAt, product.minPrice)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(auction.count());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 경매 ID와 사용자 ID로 경매 상세 정보를 조회합니다.
     *
     * @param auctionId 경매 ID
     * @param userId    사용자 ID
     * @return 경매 상세 응답
     */
    @Override
    public Optional<AuctionDetailsResponse> findAuctionDetailsById(Long auctionId, Long userId) {
        QBid activeBid = new QBid("bidActive");
        QBid canceledBid = new QBid("bidCanceled");
        Optional<AuctionDetailsResponse> auctionDetailsResponse = Optional.ofNullable(jpaQueryFactory
                .select(new QAuctionDetailsResponse(
                        product.id,
                        user.nickname,
                        user.profileImageUrl,
                        product.name,
                        product.description,
                        product.minPrice,
                        product.category,
                        timeRemaining().longValue(),
                        auction.status,
                        userIdEq(userId),
                        getBidCount(),
                        activeBid.id.isNotNull(),
                        activeBid.id,
                        activeBid.amount.coalesce(0L),
                        activeBid.count.coalesce(3),
                        canceledBid.id.isNotNull(),
                        winnerIdEq(userId), // 낙찰자 여부
                        auction.winnerId.isNotNull(), //경매의 낙찰 여부
                        order.isNotNull() // 주문 여부
                ))
                .from(auction)
                .join(auction.product, product)
                .join(product.user, user)
                // 활성화된 입찰 조인
                .leftJoin(activeBid).on(activeBid.auction.id.eq(auctionId)
                        .and(activeBid.status.eq(ACTIVE)) // ACTIVE 상태인 입찰만 조인
                        .and(bidderIdEqSub(activeBid, userId)))
                // 취소된 입찰 조인
                .leftJoin(canceledBid).on(canceledBid.auction.id.eq(auctionId)
                        .and(canceledBid.status.eq(CANCELLED)) // CANCELED 상태인 입찰 조인
                        .and(bidderIdEqSub(canceledBid, userId)))
                .leftJoin(order).on(order.auction.eq(auction))
                .where(auction.id.eq(auctionId))
                .fetchOne());

        auctionDetailsResponse.ifPresent(
                response -> response.addImageList(getImagesByProductId(response.getProductId())));

        return auctionDetailsResponse;
    }

    /**
     * 경매 ID와 사용자 ID로 경매 간단 상세 정보를 조회합니다.
     *
     * @param auctionId 경매 ID
     * @return 경매 간단 상세정보 응답
     */
    @Override
    public Optional<SimpleAuctionResponse> findSimpleAuctionDetailsById(Long auctionId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(new QSimpleAuctionResponse(
                        image.cdnPath,
                        product.name,
                        product.minPrice,
                        bid.countDistinct()
                ))
                .from(auction)
                .join(auction.product, product)
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .leftJoin(bid).on(bid.auction.id.eq(auctionId).and(bid.status.eq(ACTIVE)))
                .where(auction.id.eq(auctionId))
                .groupBy(product.name, image.cdnPath, product.minPrice)
                .fetchOne());
    }

    /**
     * 사용자 닉네임에 따라 경매 리스트를 조회합니다.
     *
     * @param nickname 사용자 닉네임
     * @param pageable 페이징 정보
     * @return 페이징된 사용자 경매 응답 리스트
     */
    @Override
    public Page<UserAuctionResponse> findAuctionsByNickname(String nickname, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.product, product)
                .join(product.user, user)
                .where(user.nickname.eq(nickname));

        return getUserAuctionResponses(pageable, baseQuery);
    }

    /**
     * 사용자 인증정보를 통해 사용자가 등록한 경매 리스트를 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사용자 경매 응답 리스트
     */
    @Override
    public Page<UserAuctionResponse> findAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.product, product)
                .join(product.user, user)
                .on(user.id.eq(userId));

        return getUserAuctionResponses(pageable, baseQuery);
    }

    private Page<UserAuctionResponse> getUserAuctionResponses(Pageable pageable, JPAQuery<?> baseQuery) {
        JPAQuery<UserAuctionResponse> contentQuery = baseQuery
                .select(new QUserAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        getBidCount(),
                        auction.status,
                        auction.createdAt));

        List<UserAuctionResponse> content = contentQuery
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 홈 화면의 베스트 경매 조회
     *
     * @return 입찰 기록이 많은 10개의 경매 정보
     */
    @Override
    public List<AuctionResponse> findBestAuctions() {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.product, product)
                .where(auction.status.eq(PROCEEDING))
                .orderBy(POPULARITY.getOrderSpecifier(), NEWEST.getOrderSpecifier());

        return baseQuery.select(new QAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        bid.countDistinct())
                )
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .leftJoin(bid).on(bid.auction.id.eq(auction.id).and(bid.status.ne(CANCELLED)))
                .groupBy(auction.id, product.name, image.cdnPath, auction.createdAt, product.minPrice)
                .offset(0)
                .limit(5)
                .fetch();
    }

    /**
     * 홈 화면의 임박 경매 조회
     *
     * @return 경매 종료까지 1시간 이내인 경매 정보
     */
    @Override
    public List<AuctionResponse> findImminentAuctions() {
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(auction)
                .join(auction.product, product)
                .where(
                        timeRemaining().between(0, 3600)
                                .and(auction.status.eq(PROCEEDING)))
                .orderBy(timeRemaining().asc(), POPULARITY.getOrderSpecifier());

        return baseQuery.select(new QAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        bid.countDistinct())
                )
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .leftJoin(bid).on(bid.auction.id.eq(auction.id).and(bid.status.ne(CANCELLED)))
                .groupBy(auction.id, product.name, image.cdnPath)
                .offset(0)
                .limit(5)
                .fetch();
    }

    /**
     * 사용자가 낙찰한 경매 이력을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 낙찰 경매 응답 리스트
     */
    @Override
    public Page<WonAuctionResponse> findWonAuctionHistoryByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(auction)
                .join(auction.bids, bid).on(bid.bidder.id.eq(userId).and(bid.status.eq(ACTIVE)))
                .join(auction.product, product)
                .where(auction.winnerId.eq(userId).and(auction.status.eq(ENDED)));

        List<WonAuctionResponse> content = baseQuery
                .select(new QWonAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        getBidCount(),
                        auction.endDateTime,
                        bid.amount,
                        order.isNotNull(),
                        order.id
                ))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .leftJoin(order).on(order.auction.id.eq(auction.id))
                .groupBy(auction.id, product.name, image.cdnPath, product.minPrice, bid.amount, order.id)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 사용자가 낙찰하지 못한 경매 이력을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 낙찰 경매 응답 리스트
     */
    @Override
    public Page<LostAuctionResponse> findLostAuctionHistoryByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(auction)
                .join(auction.bids, bid).on(bid.bidder.id.eq(userId).and(bid.status.eq(ACTIVE)))
                .where(auction.winnerId.ne(userId).and(auction.status.eq(ENDED)));

        List<LostAuctionResponse> query = baseQuery
                .select(new QLostAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        getBidCount(),
                        auction.endDateTime,
                        bid.amount
                ))
                .join(auction.product, product)
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .groupBy(auction.id, product.name, image.cdnPath, product.minPrice, auction.endDateTime)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(auction.countDistinct());

        return PageableExecutionUtils.getPage(query, pageable, countQuery::fetchCount);
    }

    @Override
    public ParticipationCountsResponse getParticipationCounts(Long userId) {
        DateTimeOperation<LocalDateTime> now = Expressions.dateTimeOperation(LocalDateTime.class,
                DateTimeOps.CURRENT_TIMESTAMP);

        BooleanExpression isEnded = auction.status.eq(ENDED)
                .and(auction.endDateTime.before(now));

        // 사용자가 참여한 경매 ID 목록을 가져옵니다.
        List<Long> participatedAuctionIds = jpaQueryFactory
                .select(auction.id)
                .from(auction)
                .join(auction.bids, bid)
                .where(bid.bidder.id.eq(userId)
                        .and(bid.status.eq(ACTIVE)))
                .fetch();

        BooleanExpression isParticipatedAuction = auction.id.in(participatedAuctionIds);

        Long proceedingCount = Optional.ofNullable(jpaQueryFactory
                        .select(auction.count())
                        .from(auction)
                        .where(isParticipatedAuction
                                .and(auction.status.eq(PROCEEDING))
                                .and(auction.endDateTime.after(now)))
                        .fetchFirst())
                .orElse(0L);

        Long successCount = Optional.ofNullable(jpaQueryFactory
                        .select(auction.count())
                        .from(auction)
                        .where(isParticipatedAuction
                                .and(auction.winnerId.eq(userId))
                                .and(isEnded))
                        .fetchFirst())
                .orElse(0L);

        Long failureCount = Optional.ofNullable(jpaQueryFactory
                        .select(auction.count())
                        .from(auction)
                        .where(isParticipatedAuction
                                .and(auction.winnerId.ne(userId))
                                .and(isEnded))
                        .fetchFirst())
                .orElse(0L);

        return new ParticipationCountsResponse(
                proceedingCount,
                successCount,
                failureCount
        );
    }

    @Override
    public Page<UserAuctionResponse> findProceedingAuctionByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(auction)
                .join(auction.product, product)
                .where(product.user.id.eq(userId).and(auction.status.eq(PROCEEDING)));

        // 진행 중인 경매 조회 쿼리
        List<UserAuctionResponse> result = baseQuery
                .select(new QUserAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        getBidCount(),
                        auction.status,
                        auction.createdAt
                ))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 경매 수를 계산하는 쿼리
        JPAQuery<Long> countQuery = baseQuery
                .select(auction.count());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<UserEndedAuctionResponse> findEndedAuctionByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(auction)
                .join(auction.product, product)
                .where(product.user.id.eq(userId).and(auction.status.eq(ENDED)));

        List<UserEndedAuctionResponse> result = baseQuery
                .select(new QUserEndedAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice.longValue(),
                        getBidCount(),
                        getWinningBidAmount(),
                        auction.winnerId.isNotNull(),
                        order.isNotNull(),
                        auction.createdAt
                ))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .leftJoin(order).on(order.auction.eq(auction))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 경매 수를 계산하는 쿼리
        JPAQuery<Long> countQuery = baseQuery
                .select(auction.count());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<WonAuctionDetailsResponse> findWinningBidById(Long auctionId) {
        return Optional.ofNullable(jpaQueryFactory.select(
                        new QWonAuctionDetailsResponse(
                                auction.id,
                                product.name,
                                image.cdnPath,
                                bid.amount
                        ))
                .from(auction)
                .join(bid)
                .on(bid.auction.eq(auction).and(auction.id.eq(auctionId)).and(auction.winnerId.eq(bid.bidder.id)))
                .join(auction.product, product)
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .fetchOne());
    }

    /**
     * 상품의 대표 이미지를 조회하기 위한 조건을 반환합니다.
     *
     * @return 대표 이미지(첫 번째 이미지)의 sequence가 1인 조건식
     */
    private BooleanExpression isRepresentativeImage() {
        return image.sequence.eq(1);
    }

    /**
     * 사용자가 참여 중인 경매인지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자가 참여 중인 경우 true, 그렇지 않으면 false
     */
    private BooleanExpression isParticipating(Long userId) {
        return JPAExpressions.selectOne()
                .from(bid)
                .where(bid.auction.id.eq(auction.id).and(bid.status.eq(ACTIVE).and(bidderIdEq(userId))))
                .exists();
    }

    /**
     * 경매 참여자 수를 조회합니다.
     *
     * @return 참여자 수
     */
    private static NumberExpression<Long> getBidCount() {
        return Expressions.asNumber(JPAExpressions
                .select(bid.count())
                .from(bid)
                .where(bid.auction.id.eq(auction.id).and(bid.status.eq(ACTIVE))));
    }

    /**
     * 상품의 이미지 리스트를 조회합니다.
     */
    private List<ImageResponse> getImagesByProductId(Long productId) {
        return jpaQueryFactory
                .select(new QImageResponse(image.id, image.cdnPath))
                .from(image)
                .where(image.product.id.eq(productId))
                .orderBy(image.sequence.asc())
                .fetch();
    }

    private static NumberExpression<Integer> timeRemaining() {
        return Expressions.numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auction.endDateTime); // 음수면 0으로 처리
    }

    /**
     * 경매의 낙찰 금액을 조회합니다.
     *
     * @return 낙찰금액
     */
    private JPQLQuery<Long> getWinningBidAmount() {
        return JPAExpressions.select(bid.amount.max().coalesce(0L))
                .from(bid)
                .where(
                        bid.auction.id.eq(auction.id),
                        bid.status.eq(ACTIVE)
                );
    }

    private BooleanBuilder userIdEq(Long userId) {
        return nullSafeBuilder(() -> user.id.eq(userId));
    }

    private BooleanBuilder bidderIdEq(Long userId) {
        return nullSafeBuilder(() -> bid.bidder.id.eq(userId));
    }

    private BooleanBuilder bidderIdEqSub(QBid qBid, Long userId) {
        return nullSafeBuilder(() -> qBid.bidder.id.eq(userId));
    }

    private BooleanBuilder winnerIdEq(Long userId) {
        return nullSafeBuilder(() -> auction.winnerId.isNotNull().and(auction.winnerId.eq(userId)));
    }

    private BooleanBuilder buyerIdEq(Long userId) {
        return nullSafeBuilder(() -> order.buyerId.eq(userId));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum AuctionOrder implements QuerydslOrder {
        POPULARITY("popularity", getBidCount().desc()),
        EXPENSIVE("expensive", product.minPrice.desc()),
        CHEAP("cheap", product.minPrice.asc()),
        NEWEST("newest", auction.createdAt.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }
}
