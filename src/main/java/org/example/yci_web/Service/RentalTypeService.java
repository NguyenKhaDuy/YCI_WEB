package org.example.yci_web.Service;

import org.example.yci_web.Model.DTO.RentalTypeDTO;
import org.example.yci_web.Model.Request.RentalTypeRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;

import java.util.List;

public interface RentalTypeService {
    DataResponse getAllRentalTypes();
    Object getRentalTypeById(Long idType);
    MessageResponse addRentalType(RentalTypeRequest rentalTypeRequest);
    MessageResponse updateRentalType(RentalTypeRequest rentalTypeRequest);
    MessageResponse deleteRentalType(Long idType);
}
