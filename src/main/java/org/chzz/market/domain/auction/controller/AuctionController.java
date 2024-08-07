package org.chzz.market.domain.auction.controller;

import java.net.URI;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.request.AuctionCreateRequest;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.auction.entity.SortType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auctions")
public class AuctionController {
    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<Void> createAuction(@ModelAttribute @Valid AuctionCreateRequest dto) {
        Long auctionId = auctionService.createAuction(dto);
        return ResponseEntity
                .created(URI.create("/api/v1/auctions/" + auctionId))
                .build();
    }

    @GetMapping
    public ResponseEntity<?> getAuctionList(@RequestParam Category category,
//                                            @AuthenticationPrincipal CustomUserDetails customUserDetails, // TODO: 추후에 인증된 사용자 정보로 수정 필요
                                            @RequestParam(defaultValue = "newest") SortType type,
                                            Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByCategory(category, type, 1L, pageable)); // 임의의 사용자 ID
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionDetails(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuctionDetails(auctionId, 1L)); // TODO: 추후에 인증된 사용자 정보로 수정 필요
    }
}
