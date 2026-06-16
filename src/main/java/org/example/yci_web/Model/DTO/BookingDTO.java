package org.example.yci_web.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long idBooking;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timeStart;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime timeEnd;
    private Double totalAmount;
    private String note;
    private String status;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    private List<BookingDetailDTO> bookingDetailDTOS;
    private DepositTypeDTO depositTypeDTO;
    private UserBookingDTO userBookingDTO;
    private DocumentBookingDTO documentBookingDTO;
    private List<PaymentBookingDTO> paymentBookingDTOS;
}
