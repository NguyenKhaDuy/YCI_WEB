package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "return_detail")
public class ReturnDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReturnDetail;

    @Column(name = "returned_quantity")
    private Integer returnedQuantity;

    @Column(name = "late_hours")
    private Integer lateHours;

    @Column(name = "late_fee")
    private Double lateFee;

    @Column(name = "cleaning_fee")
    private Double cleaningFee;

    @Column(name = "damage_fee")
    private Double damageFee;

    @Column(name = "subtotal_fee")
    private Double subtotalFee;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_return")
    private ReturnEntity returnEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_booking_detail")
    private BookingDetailEntity bookingDetailEntity ;
}

