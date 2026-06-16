package org.example.yci_web.Controller;

import org.example.yci_web.Model.DTO.DocumentDTO;
import org.example.yci_web.Model.Request.UpdateStatusDocumentRequest;
import org.example.yci_web.Model.Response.DataPageResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController {
    @Autowired
    DocumentService documentService;

    @GetMapping(value = "/api/admin/document")
    public ResponseEntity<Object> getDocument(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<DocumentDTO> documentDTOS = documentService.getDocuments(page);
        DataPageResponse dataPageResponse = new DataPageResponse();
        dataPageResponse.setData(documentDTOS.getContent());
        dataPageResponse.setMessage("success");
        dataPageResponse.setTotalPage(documentDTOS.getTotalPages());
        dataPageResponse.setCurrentPage(page);
        dataPageResponse.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(dataPageResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/document/id={id}")
    public ResponseEntity<Object> getDocumentById(@PathVariable(name = "id") Long id) {
        Object result = documentService.getDocumentById(id);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/document")
    public ResponseEntity<Object> updateStatusDocument(@RequestBody UpdateStatusDocumentRequest updateStatusDocumentRequest) {
        MessageResponse messageResponse = documentService.updateStatusDoccument(updateStatusDocumentRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @DeleteMapping(value = "/api/admin/document/id={id}")
    public ResponseEntity<Object> deleteDocument(@PathVariable(name = "id") Long id) {
        MessageResponse messageResponse = documentService.deleteDocumentById(id);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

}
