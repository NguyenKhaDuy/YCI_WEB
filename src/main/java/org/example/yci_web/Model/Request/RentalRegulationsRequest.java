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
public class RentalRegulationsRequest {
    private Long idRentalRegulations;
    private String regulation;
}
