package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_ACCESSIBLE;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import org.chzz.market.domain.auction.dto.AuctionDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.auction.entity.SortType;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionRepository auctionRepository;

    /**
     * 경매 상품 진행 상태로 저장
     */
    public void proceedingAuctionForProduct(Product product) {
        Auction auction = Auction.builder()
                .product(product)
                .minPrice(product.getMinPrice())
                .status(Auction.AuctionStatus.PROCEEDING)
                .build();

        auctionRepository.save(auction);
    }

    /**
     * 경매 상품 대기 상태로 저장
     */
    public void pendingAuctionForProduct(Product product) {
        Auction auction = Auction.builder()
                .product(product)
                .minPrice(product.getMinPrice())
                .status(Auction.AuctionStatus.PENDING)
                .build();

        auctionRepository.save(auction);
    }

    /**
     * 경매 대기 상품 -> 진행 상태로 전환
     */
    public void convertPendingToProceeding(Product product){
        // 경매 상품 유효성 검사
        Auction auction = auctionRepository.findByProduct(product)
                .orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

        // 경매 상품 상태 유효성 검사
        if (auction.getStatus() != Auction.AuctionStatus.PENDING) {
            throw new AuctionException(AuctionErrorCode.INVALID_AUCTION_STATE);
        }

        // 경매 상품 상태 전환 및 저장
        auction.convertToProceeding();
        auctionRepository.save(auction);
    }

    public Auction getAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
    }

    public Page<AuctionResponse> getAuctionListByCategory(Category category, SortType sortType, Long userId,
                                                      Pageable pageable) {
        return auctionRepository.findAuctionsByCategory(category, sortType, userId, pageable);
    }

    public AuctionDetailsResponse getAuctionDetails(Long auctionId, Long userId) {
        Optional<AuctionDetailsResponse> auctionDetails = auctionRepository.findAuctionDetailsById(auctionId, userId);
        return auctionDetails.orElseThrow(() -> new AuctionException(AUCTION_NOT_ACCESSIBLE));
    }

}
