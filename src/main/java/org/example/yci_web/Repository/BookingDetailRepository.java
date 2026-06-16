package org.example.yci_web.Repository;

import org.example.yci_web.Entity.BookingDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetailEntity, Long> {
}
