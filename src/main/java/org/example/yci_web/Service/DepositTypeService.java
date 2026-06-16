package org.example.yci_web.Service;

import org.example.yci_web.Model.Request.DepositTypeRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;

public interface DepositTypeService {
    DataResponse getAllDepositTypes();
    Object getDepositTypeById(Long id);
    MessageResponse addDepositType(DepositTypeRequest depositTypeRequest);
    MessageResponse updateDepositType(DepositTypeRequest depositTypeRequest);
    MessageResponse deleteDepositType(Long id);
}
