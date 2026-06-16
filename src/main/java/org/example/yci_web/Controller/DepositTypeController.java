package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.DepositTypeRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.DepositTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DepositTypeController {
    @Autowired
    DepositTypeService depositTypeService;

    @GetMapping(value = "/api/deposit-type")
    public ResponseEntity<Object> getDepositType(){
        DataResponse dataResponse = depositTypeService.getAllDepositTypes();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/deposit-type/id={id}")
    public ResponseEntity<Object> getDepositTypeById(@PathVariable("id") Long id){
        Object result = depositTypeService.getDepositTypeById(id);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/deposit-type")
    public ResponseEntity<Object> addDepositType(@RequestBody DepositTypeRequest depositTypeRequest){
        MessageResponse messageResponse = depositTypeService.addDepositType(depositTypeRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/deposit-type")
    public ResponseEntity<Object> updateDepositType(@RequestBody DepositTypeRequest depositTypeRequest){
        MessageResponse messageResponse = depositTypeService.updateDepositType(depositTypeRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/api/admin/deposit-type/id={id}")
    public ResponseEntity<Object> deleteDepositType(@PathVariable(name = "id") Long id){
        MessageResponse messageResponse = depositTypeService.deleteDepositType(id);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }
}
