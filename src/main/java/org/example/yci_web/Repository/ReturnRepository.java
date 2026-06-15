package org.example.yci_web.Repository;

import org.example.yci_web.Entity.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<ReturnEntity, Long> {
}
