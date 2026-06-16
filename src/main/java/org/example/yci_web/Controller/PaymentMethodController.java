package org.example.yci_web.Controller;

import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentMethodController {
    @Autowired
    PaymentMethodService paymentMethodService;

    @GetMapping(value = "/api/payment-method")
    public ResponseEntity<Object> getPaymentMethod() {
        DataResponse dataResponse = paymentMethodService.getPaymentMethods();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }
}
