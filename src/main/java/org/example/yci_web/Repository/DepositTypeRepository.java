package org.example.yci_web.Repository;

import org.example.yci_web.Entity.DepositTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositTypeRepository extends JpaRepository<DepositTypeEntity, Long> {
}
