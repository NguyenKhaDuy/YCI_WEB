package org.example.yci_web.Repository;

import org.example.yci_web.Entity.DocumentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentsRepository extends JpaRepository<DocumentsEntity, Long> {
}
