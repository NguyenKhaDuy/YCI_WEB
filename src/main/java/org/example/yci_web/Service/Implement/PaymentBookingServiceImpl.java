package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.BookingEntity;
import org.example.yci_web.Entity.PaymentBookingEntity;
import org.example.yci_web.Entity.PaymentsMethodEntity;
import org.example.yci_web.Model.Request.PaymentBookingRequest;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.BookingRepository;
import org.example.yci_web.Repository.PaymentBookingRepository;
import org.example.yci_web.Repository.PaymentsMethodRepository;
import org.example.yci_web.Service.PaymentBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PaymentBookingServiceImpl implements PaymentBookingService {
    @Autowired
    PaymentBookingRepository paymentBookingRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    PaymentsMethodRepository paymentsMethodRepository;

    @Override
    public MessageResponse addPaymentBooking(PaymentBookingRequest paymentBookingRequest) {
        MessageResponse messageResponse = new MessageResponse();
        BookingEntity bookingEntity = null;
        PaymentsMethodEntity paymentsMethodEntity = null;
        try {
            bookingEntity = bookingRepository.findById(paymentBookingRequest.getBookingId()).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Booking not found");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        try {
            paymentsMethodEntity = paymentsMethodRepository.findById(paymentBookingRequest.getPaymentMethodId()).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Booking not found");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        List<PaymentBookingEntity> paymentBookingEntities = paymentBookingRepository.findByBookingEntity(bookingEntity);
        Double totalPriceRemain = 0.0;
        for (PaymentBookingEntity paymentBooking : paymentBookingEntities) {
            totalPriceRemain = totalPriceRemain + paymentBooking.getRemainAmount();
        }
        PaymentBookingEntity paymentBookingEntity = new PaymentBookingEntity();
        paymentBookingEntity.setBookingEntity(bookingEntity);
        paymentBookingEntity.setPaymentsMethodEntity(paymentsMethodEntity);
        paymentBookingEntity.setPaidAt(LocalDateTime.now());
        paymentBookingEntity.setPrice(paymentBookingRequest.getPrice());
        paymentBookingEntity.setRemainAmount(paymentBookingRequest.getPrice() - totalPriceRemain);
        paymentBookingRepository.save(paymentBookingEntity);
        messageResponse.setMessage("Payment booking added");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }
}
