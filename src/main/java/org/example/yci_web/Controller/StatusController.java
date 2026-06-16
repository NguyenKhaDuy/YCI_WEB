package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.StatusRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class StatusController {
    @Autowired
    StatusService statusService;

    @GetMapping(value = "/api/admin/status")
    public ResponseEntity<Object> getAllStatus() {
        DataResponse response = statusService.getAllStatus();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/status/id={idStatus}")
    public ResponseEntity<Object> getStatusById(@PathVariable("idStatus") Long idStatus) {
        Object result = statusService.getStatusById(idStatus);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/status")
    public ResponseEntity<Object> addStatus(@RequestBody StatusRequest statusRequest) {
        MessageResponse response = statusService.addStatus(statusRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/status")
    public ResponseEntity<Object> updateStatus(@RequestBody StatusRequest statusRequest) {
        MessageResponse response = statusService.updateStatus(statusRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/status/id={idStatus}")
    public ResponseEntity<Object> deleteStatus(@PathVariable("idStatus") Long idStatus) {
        MessageResponse response = statusService.deleteStatus(idStatus);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
