package org.example.yci_web.Model.Request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private Long idProduct;
    private String nameProduct;
    private String brand;
    private String description;
    private String serialNumber;
    private Double depositPrice;
    private Long idCategory;
    private List<MultipartFile> images;
}
