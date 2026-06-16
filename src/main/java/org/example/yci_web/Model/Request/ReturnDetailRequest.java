package org.example.yci_web.Model.Request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnDetailRequest {
    private Integer returnedQuantity;
    private Integer lateHours;
    private Double lateFee;
    private Double cleaningFee;
    private Double damageFee;
    private Double subtotalFee;
    private String description;
    private Long bookingDetailId;
    private Long statusId;
}
