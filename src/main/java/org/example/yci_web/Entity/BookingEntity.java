package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBooking;

    @Column(name = "time_start")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timeStart;

    @Column(name = "time_end")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timeEnd;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "note")
    private String note;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "paid_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_deposit_type")
    private DepositTypeEntity depositTypeEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_payment_method")
    private PaymentsMethodEntity paymentsMethodEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status")
    private StatusEntity statusEntity;

    @OneToMany(mappedBy = "bookingEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<BookingDetailEntity> bookingDetailEntities = new ArrayList<>();

    @OneToMany(mappedBy = "bookingEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<DocumentsEntity> documentsEntities = new ArrayList<>();

    @OneToMany(mappedBy = "bookingEntity", fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<PaymentBookingEntity> paymentBookingEntities = new ArrayList<>();
}
