package org.example.yci_web.Service;

import org.example.yci_web.Model.DTO.StatusDTO;
import org.example.yci_web.Model.Request.StatusRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;

import java.util.List;

public interface StatusService {
    DataResponse getAllStatus();
    Object getStatusById(Long idStatus);
    MessageResponse addStatus(StatusRequest statusRequest);
    MessageResponse updateStatus(StatusRequest statusRequest);
    MessageResponse deleteStatus(Long idStatus);
}
