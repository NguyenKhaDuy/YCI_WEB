package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_booking")
public class PaymentBookingEntity {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long idPaymentBooking;

    @Column(name = "paid_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime paidAt;

    @Column(name = "price")
    private Double price;

    @Column(name = "remain_amount")
    private Double remainAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_booking")
    private BookingEntity bookingEntity;
}
