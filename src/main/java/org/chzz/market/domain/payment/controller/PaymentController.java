package org.chzz.market.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.ApprovalResponse;
import org.chzz.market.domain.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/approval")
    public ResponseEntity<?> approvePayment(@RequestBody ApprovalRequest request){
        ApprovalResponse approval = paymentService.approval(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(approval);
    }

    @PostMapping("/order-id")
    public ResponseEntity<?> createOrderId(){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createOrderId());
    }
}
