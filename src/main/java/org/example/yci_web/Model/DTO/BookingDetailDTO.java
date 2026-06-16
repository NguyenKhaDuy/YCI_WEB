package org.example.yci_web.Model.DTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailDTO {
    private Long idBookingDetail;
    private Double totalPrice;
    private ProductBookingDTO productBookingDTO;
    private ReturnDetailDTO returnDetailDTO;
}
