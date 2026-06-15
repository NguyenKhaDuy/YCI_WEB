package org.example.yci_web.Repository;

import org.example.yci_web.Entity.PaymentsMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentsMethodRepository extends JpaRepository<PaymentsMethodEntity, Long> {
}
