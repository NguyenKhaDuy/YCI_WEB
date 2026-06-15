package org.example.yci_web.Repository;

import org.example.yci_web.Entity.PaymentBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentBookingRepository extends JpaRepository<PaymentBookingEntity, Long> {
}
