package org.example.yci_web.Service;


import org.example.yci_web.Model.DTO.DocumentBookingDTO;
import org.example.yci_web.Model.DTO.DocumentDTO;
import org.example.yci_web.Model.Request.UpdateStatusDocumentRequest;
import org.example.yci_web.Model.Response.MessageResponse;
import org.springframework.data.domain.Page;

public interface DocumentService {
    Page<DocumentDTO> getDocuments(Integer page);
    Object getDocumentById(Long idDocument);
    MessageResponse updateStatusDoccument(UpdateStatusDocumentRequest updateStatusDocumentRequest);
    MessageResponse deleteDocumentById(Long idDocument);
}
