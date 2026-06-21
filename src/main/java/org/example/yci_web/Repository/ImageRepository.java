package org.example.yci_web.Repository;

import org.example.yci_web.Entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByProductEntity_IdProductOrderByIdImageAsc(Long idProduct);
    Optional<ImageEntity> findFirstByProductEntity_IdProductOrderByIdImageAsc(Long idProduct);
    Optional<ImageEntity> findByIdImageAndProductEntity_IdProduct(Long idImage, Long idProduct);
}
