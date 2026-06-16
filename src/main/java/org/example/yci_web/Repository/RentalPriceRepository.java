package org.example.yci_web.Repository;

import org.example.yci_web.Entity.ProductEntity;
import org.example.yci_web.Entity.RentalPriceEntity;
import org.example.yci_web.Entity.RentalTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalPriceRepository extends JpaRepository<RentalPriceEntity, Long> {
    RentalPriceEntity findByProductEntityAndRentalTypeEntity(ProductEntity productEntity, RentalTypeEntity rentalType);
}
