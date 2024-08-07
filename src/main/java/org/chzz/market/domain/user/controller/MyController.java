package org.chzz.market.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.response.MyAuctionResponse;
import org.chzz.market.domain.auction.service.AuctionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class MyController {
    private final AuctionService auctionService;

    @GetMapping("/auctions")
    public ResponseEntity<Page<MyAuctionResponse>> getMyAuctionList(@PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByUserId(1L, pageable)); // TODO: 추후에 인증된 사용자 정보로 수정 필요
    }
}
