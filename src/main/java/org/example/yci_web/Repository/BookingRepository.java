package org.example.yci_web.Repository;

import org.example.yci_web.Entity.BookingEntity;
import org.example.yci_web.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByUserEntity(UserEntity userEntity);
}
