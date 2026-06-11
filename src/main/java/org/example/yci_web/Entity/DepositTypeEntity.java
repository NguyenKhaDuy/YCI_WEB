package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "deposit_type")
public class DepositTypeEntity {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long idDepositType;

    @Column(name = "type")
    private String type;

    @Column(name = "percent_deposit")
    private Integer percentDeposit;

    @OneToMany(mappedBy = "depositTypeEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<BookingEntity> bookingEntities = new ArrayList<>();
}
