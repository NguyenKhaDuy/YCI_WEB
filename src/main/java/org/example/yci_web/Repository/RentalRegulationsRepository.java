package org.example.yci_web.Repository;

import org.example.yci_web.Entity.RentalRegulationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRegulationsRepository extends JpaRepository<RentalRegulationsEntity, Long> {
}
