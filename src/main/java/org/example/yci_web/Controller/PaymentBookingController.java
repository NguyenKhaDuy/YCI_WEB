package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.PaymentBookingRequest;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.PaymentBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentBookingController {
    @Autowired
    PaymentBookingService paymentBookingService;

    @PostMapping(value = "/api/admin/payment-booking")
    public ResponseEntity<Object> addPaymentBooking(@RequestBody PaymentBookingRequest paymentBookingRequest){
        MessageResponse messageResponse = paymentBookingService.addPaymentBooking(paymentBookingRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }
}
