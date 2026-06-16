package org.example.yci_web.Controller;

import org.example.yci_web.Model.DTO.BookingDTO;
import org.example.yci_web.Model.Request.AcceptBookingRequest;
import org.example.yci_web.Model.Request.BookingRequest;
import org.example.yci_web.Model.Request.UpdateStatusBookingRequest;
import org.example.yci_web.Model.Response.DataPageResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BookingController {
    @Autowired
    BookingService bookingService;

    @PostMapping(value = "/api/booking")
    public ResponseEntity<Object> booking(@ModelAttribute BookingRequest bookingRequest) {
        MessageResponse messageResponse = bookingService.Booking(bookingRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @GetMapping(value = "/api/admin/booking")
    public ResponseEntity<Object> getBooking(@RequestParam(name = "page", defaultValue = "1") Integer page) {
        Page<BookingDTO> bookingDTOS = bookingService.getBookings(page);
        DataPageResponse dataPageResponse = new DataPageResponse();
        dataPageResponse.setData(bookingDTOS.getContent());
        dataPageResponse.setCurrentPage(page);
        dataPageResponse.setTotalPage(bookingDTOS.getTotalPages());
        dataPageResponse.setMessage("Success");
        dataPageResponse.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(dataPageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/booking/accept")
    public ResponseEntity<Object> bookingAccept(@RequestBody AcceptBookingRequest acceptBookingRequest) {
        MessageResponse messageResponse = bookingService.acceptBooking(acceptBookingRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @PutMapping(value = "/api/admin/booking/status")
    public ResponseEntity<Object> updateBookingStatus(@RequestBody UpdateStatusBookingRequest updateStatusBookingRequest) {
        MessageResponse messageResponse = bookingService.updateStatusBooking(updateStatusBookingRequest);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @GetMapping(value = "/api/booking/id={id}")
    public ResponseEntity<Object> getBookingById(@PathVariable("id") Long id) {
        Object result = bookingService.getBookingById(id);
        if(result instanceof MessageResponse) {
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/api/booking/id-user={idUser}")
    public ResponseEntity<Object> getBookingByIdUser(@PathVariable("idUser") Long idUser) {
        Object result = bookingService.getBookingsByUser(idUser);
        if(result instanceof MessageResponse) {
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
