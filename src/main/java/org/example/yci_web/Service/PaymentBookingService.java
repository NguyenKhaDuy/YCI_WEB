package org.example.yci_web.Service;

import org.example.yci_web.Model.Request.PaymentBookingRequest;
import org.example.yci_web.Model.Response.MessageResponse;

public interface PaymentBookingService {
    MessageResponse addPaymentBooking(PaymentBookingRequest paymentBookingRequest);
}
