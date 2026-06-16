package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.BookingDetailEntity;
import org.example.yci_web.Entity.ReturnDetailEntity;
import org.example.yci_web.Entity.StatusEntity;
import org.example.yci_web.Model.Request.ReturnDetailRequest;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.BookingDetailRepository;
import org.example.yci_web.Repository.ReturnDetailRepository;
import org.example.yci_web.Repository.StatusRepository;
import org.example.yci_web.Service.ReturnDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ReturnDetailServiceImpl implements ReturnDetailService {
    @Autowired
    ReturnDetailRepository returnDetailRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    BookingDetailRepository bookingDetailRepository;

    @Override
    public MessageResponse addReturnDetail(ReturnDetailRequest returnDetailRequest) {
        MessageResponse messageResponse = new MessageResponse();
        BookingDetailEntity bookingDetailEntity = null;
        StatusEntity statusEntity = null;
        try{
            bookingDetailEntity = bookingDetailRepository.findById(returnDetailRequest.getBookingDetailId()).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Booking Detail Not Found");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        try{
            statusEntity = statusRepository.findById(returnDetailRequest.getStatusId()).get();
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Booking Detail Not Found");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        ReturnDetailEntity returnDetailEntity = new ReturnDetailEntity();
        returnDetailEntity.setReturnedQuantity(returnDetailRequest.getReturnedQuantity());
        returnDetailEntity.setDescription(returnDetailRequest.getDescription());
        returnDetailEntity.setLateFee(returnDetailRequest.getLateFee());
        returnDetailEntity.setDamageFee(returnDetailRequest.getDamageFee());
        returnDetailEntity.setCleaningFee(returnDetailRequest.getCleaningFee());
        returnDetailEntity.setLateHours(returnDetailRequest.getLateHours());
        returnDetailEntity.setSubtotalFee(returnDetailRequest.getSubtotalFee());
        bookingDetailEntity.getProductEntity().setStatusEntity(statusEntity);
        returnDetailEntity.setBookingDetailEntity(bookingDetailEntity);
        returnDetailRepository.save(returnDetailEntity);
        messageResponse.setMessage("Return Detail Added");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }
}
