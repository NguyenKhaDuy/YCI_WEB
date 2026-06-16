package org.example.yci_web.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnDetailDTO {
    private Long idReturnDetail;
    private Integer returnedQuantity;
    private Integer lateHours;
    private Double lateFee;
    private Double cleaningFee;
    private Double damageFee;
    private Double subtotalFee;
    private String description;
}
