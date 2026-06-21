package org.example.yci_web.Service;

import org.example.yci_web.Model.DTO.BookingDTO;
import org.example.yci_web.Model.Request.AcceptBookingRequest;
import org.example.yci_web.Model.Request.BookingRequest;
import org.example.yci_web.Model.Request.UpdateStatusBookingRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookingService {
    MessageResponse Booking(BookingRequest bookingRequest);
    Page<BookingDTO> getBookings(Integer page);
    MessageResponse acceptBooking(AcceptBookingRequest acceptBookingRequest);
    MessageResponse updateStatusBooking(UpdateStatusBookingRequest updateStatusBookingRequest);
    MessageResponse cancelBooking(Long idBooking, Long idUser);
    Object getBookingsByUser(Long idUser);
    Object getBookingById(Long idBooking);
}
