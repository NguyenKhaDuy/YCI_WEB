package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.ReturnDetailRequest;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.ReturnDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReturnDetailController {
    @Autowired
    ReturnDetailService returnDetailService;

    @PostMapping(value = "/api/admin/return-detail")
    public ResponseEntity<Object> addReturnDetail(@RequestBody ReturnDetailRequest returnDetailRequest){
        MessageResponse messageResponse = returnDetailService.addReturnDetail(returnDetailRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }
}
