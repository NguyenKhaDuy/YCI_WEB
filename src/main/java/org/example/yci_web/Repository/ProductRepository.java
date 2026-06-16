package org.example.yci_web.Repository;

import org.example.yci_web.Entity.CategoryEntity;
import org.example.yci_web.Entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByCategoryEntity(CategoryEntity categoryEntity);
}
