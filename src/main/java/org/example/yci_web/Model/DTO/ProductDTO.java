package org.example.yci_web.Model.DTO;

import jakarta.persistence.Column;
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
public class ProductDTO {
    private Long idProduct;
    private String nameProduct;
    private String brand;
    private String description;
    private String serialNumber;
    private Double depositPrice;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;
    private Long categoryId;
    private String categoryName;
    private List<ImageDTO> imageDTOS;
    private String status;
    private List<RentalPriceDTO> rentalPriceDTOS;
}
