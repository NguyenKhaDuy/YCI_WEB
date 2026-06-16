package org.example.yci_web.Repository;

import org.example.yci_web.Entity.RentalTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalTypeRepository extends JpaRepository<RentalTypeEntity, Long> {
}
