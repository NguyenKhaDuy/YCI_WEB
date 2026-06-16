package org.example.yci_web.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentBookingDTO {
    private Long idDocument;
    private String documentType;
    private String documentNumber;
    private String imageFront;
    private String imageBack;
    private String status;
}
