package org.example.yci_web.Service;

import org.example.yci_web.Model.Request.RentalRegulationsRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;


public interface RentalRegulationsService {
    DataResponse getRentalRegulations();
    Object getRentalRegulationsById(Long idRentalRegulations);
    MessageResponse addRentalRegulations(RentalRegulationsRequest rentalRegulationsRequest);
    MessageResponse deleteRentalRegulations(Long idRentalRegulations);
    MessageResponse updateRentalRegulations(RentalRegulationsRequest rentalRegulationsRequest);
}
