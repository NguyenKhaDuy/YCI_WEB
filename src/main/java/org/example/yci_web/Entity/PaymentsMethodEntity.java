package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "payments_method")
public class PaymentsMethodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPaymentsMethod;

    @Column(name = "method")
    private String method;

    @OneToMany(mappedBy = "paymentsMethodEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<BookingEntity> bookingEntities = new ArrayList<>();
}
