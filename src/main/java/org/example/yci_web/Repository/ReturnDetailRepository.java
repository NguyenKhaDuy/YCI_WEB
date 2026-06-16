package org.example.yci_web.Repository;

import org.example.yci_web.Entity.BookingDetailEntity;
import org.example.yci_web.Entity.ReturnDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnDetailRepository extends JpaRepository<ReturnDetailEntity, Long> {
    ReturnDetailEntity findByBookingDetailEntity(BookingDetailEntity bookingDetailEntity);
}
