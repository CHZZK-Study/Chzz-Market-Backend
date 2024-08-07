package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ENDED;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BELOW_MIN_PRICE;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BY_OWNER;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {
    private final AuctionService auctionService;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createBid(final BidCreateRequest bidCreateRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
        Auction auction = auctionService.getAuction(bidCreateRequest.getAuctionId());
        validateBidConditions(bidCreateRequest, user.getId(), auction);
        bidRepository.findByAuctionAndBidder(auction, user)
                .ifPresentOrElse(
                        // 이미 입찰을 한 경우
                        bid -> bid.adjustBidAmount(bidCreateRequest.getAmount()),
                        // 입찰을 처음 하는 경우
                        () -> bidRepository.save(bidCreateRequest.toEntity(auction, user))
                );
    }

    private void validateBidConditions(BidCreateRequest bidCreateRequest, Long userId, Auction auction) {
        // 경매 등록자가 입찰할 때
        if (auction.getProduct().getUser().getId() == userId) {
            throw new BidException(BID_BY_OWNER);
        }
        // 경매가 진행중이 아닐 때
        if (!auction.isProceeding() || auction.isEnded()) {
            throw new AuctionException(AUCTION_ENDED);
        }
        // 최소 금액보다 낮은 금액일 때
        if (!auction.isAboveMinPrice(bidCreateRequest.getAmount())) {
            throw new BidException(BID_BELOW_MIN_PRICE);
        }
    }
}
