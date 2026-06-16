package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.RentalRegulationsRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.RentalRegulationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RentalRegulationsController {
    @Autowired
    RentalRegulationsService rentalRegulationsService;

    @GetMapping(value = "/api/admin/rental-regulation")
    public ResponseEntity<Object> getAllRentalRegulation(){
        DataResponse dataResponse = rentalRegulationsService.getRentalRegulations();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/rental-regulation/id={id}")
    public ResponseEntity<Object> getRentalRegulationById(@PathVariable("id") Long id){
        Object result = rentalRegulationsService.getRentalRegulationsById(id);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/rental-regulation")
    public ResponseEntity<Object> addRentalRegulation(@RequestBody RentalRegulationsRequest rentalRegulationsRequest){
        MessageResponse messageResponse = rentalRegulationsService.addRentalRegulations(rentalRegulationsRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/rental-regulation")
    public ResponseEntity<Object> updateRentalRegulation(@RequestBody RentalRegulationsRequest rentalRegulationsRequest){
        MessageResponse messageResponse = rentalRegulationsService.updateRentalRegulations(rentalRegulationsRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @DeleteMapping(value = "/api/admin/rental-regulation/id={id}")
    public ResponseEntity<Object> deleteRentalRegulation(@PathVariable("id") Long id){
        MessageResponse messageResponse = rentalRegulationsService.deleteRentalRegulations(id);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }
}
