package org.example.yci_web.Model.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RentalPriceProductRequest {
    private Long productId;
    private Long typeId;
    private Double price;
}
