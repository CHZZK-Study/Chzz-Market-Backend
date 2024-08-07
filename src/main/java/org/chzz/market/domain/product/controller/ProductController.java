package org.chzz.market.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.request.RegisterProductRequest;
import org.chzz.market.domain.product.dto.response.RegisterProductResponse;
import org.chzz.market.domain.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    // 경매 등록
    @PostMapping(value = "/register",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterProductResponse> registerAuctionProduct(@ModelAttribute @Valid RegisterProductRequest request) {
        RegisterProductResponse response = productService.registerAuctionProduct(request);
        logger.info("Successfully registered auction product. Product ID: {}", response.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 사전 등록
    @PostMapping(value = "/pre-register",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterProductResponse> preRegisterProduct(@ModelAttribute @Valid RegisterProductRequest request) {
        RegisterProductResponse response = productService.preRegisterProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 경매 상품으로 전환
    @PostMapping("/{productId}/convert-to-auction")
    public ResponseEntity<RegisterProductResponse> convertToAuction(@PathVariable Long productId) {
        RegisterProductResponse response = productService.convertToAuction(productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
