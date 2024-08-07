package org.chzz.market.domain.payment.repository;

import org.chzz.market.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Boolean existsByOrderId(String orderId);
}
