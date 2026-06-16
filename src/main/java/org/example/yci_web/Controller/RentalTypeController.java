package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.RentalTypeRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.RentalTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RentalTypeController {
    @Autowired
    RentalTypeService rentalTypeService;

    @GetMapping(value = "/api/admin/rental-type")
    public ResponseEntity<Object>  getAllRentalType(){
        DataResponse dataResponse = rentalTypeService.getAllRentalTypes();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/rental-type/id={id}")
    public ResponseEntity<Object> getRentalTypeById(@PathVariable("id") Long id){
        Object result = rentalTypeService.getRentalTypeById(id);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/rental-type")
    public ResponseEntity<Object> addRentalType(@RequestBody RentalTypeRequest rentalTypeRequest){
        MessageResponse messageResponse = rentalTypeService.addRentalType(rentalTypeRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/rental-type")
    public ResponseEntity<Object> updateRentalType(@RequestBody RentalTypeRequest rentalTypeRequest){
        MessageResponse messageResponse = rentalTypeService.updateRentalType(rentalTypeRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @DeleteMapping(value = "/api/admin/rental-type/id={id}")
    public ResponseEntity<Object> deleteRentalType(@PathVariable("id") Long id){
        MessageResponse messageResponse = rentalTypeService.deleteRentalType(id);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

}
