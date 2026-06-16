package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rental_type")
public class RentalTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRentalType;

    @Column(name = "type")
    private String type;

    @OneToMany(mappedBy = "rentalTypeEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<RentalPriceEntity> rentailPriceEntities = new ArrayList<>();
}
