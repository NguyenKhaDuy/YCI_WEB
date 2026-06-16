package org.example.yci_web.Model.DTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductBookingDTO {
    private Long idProduct;
    private String nameProduct;
    private String brand;
    private String serialNumber;
    private Double price;
}
