package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.*;
import org.example.yci_web.Model.DTO.*;
import org.example.yci_web.Model.Request.AcceptBookingRequest;
import org.example.yci_web.Model.Request.BookingRequest;
import org.example.yci_web.Model.Request.UpdateStatusBookingRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.*;
import org.example.yci_web.Service.BookingService;
import org.example.yci_web.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingServiceImpl implements BookingService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    RentalPriceRepository rentalPriceRepository;
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    RentalTypeRepository rentalTypeRepository;
    @Autowired
    PaymentsMethodRepository paymentsMethodRepository;
    @Autowired
    DepositTypeRepository depositTypeRepository;
    @Autowired
    PaymentBookingRepository paymentBookingRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public MessageResponse Booking(BookingRequest bookingRequest) {
        MessageResponse messageResponse = new MessageResponse();
        UserEntity userEntity = null;
        BookingEntity bookingEntity = new BookingEntity();
        DocumentsEntity documentsEntity = new DocumentsEntity();
        StatusEntity statusEntity = null;
        RentalTypeEntity rentalTypeEntity = null;
        List<BookingDetailEntity> bookingDetailEntities = new ArrayList<>();

        //kiem tra xem co trung lich hay khong
        List<BookingEntity> bookings = bookingRepository.findAll();
        for (BookingEntity booking : bookings) {

            boolean overlap =
                    bookingRequest.getTimeStart().isBefore(booking.getTimeEnd())
                            &&
                            bookingRequest.getTimeEnd().isAfter(booking.getTimeStart());

            if (overlap) {
                messageResponse.setMessage("Can not booking");
                messageResponse.setStatus(HttpStatus.BAD_REQUEST);
                return messageResponse;
            }
        }

        try {
            userEntity = userRepository.findById(bookingRequest.getUserId()).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("No such user");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        try {
            statusEntity = statusRepository.findByStatusCode("WAITING ACCEPT");
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("No such payment method");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        try {
            rentalTypeEntity = rentalTypeRepository.findById(bookingRequest.getIdRentalType()).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("No such rental type");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
//        modelMapper.map(bookingRequest, bookingEntity);
        bookingEntity.setTimeStart(bookingRequest.getTimeStart());
        bookingEntity.setTimeEnd(bookingRequest.getTimeEnd());
        bookingEntity.setNote(bookingRequest.getNote());
        bookingEntity.setTotalAmount(bookingRequest.getTotalAmount());
        bookingEntity.setUserEntity(userEntity);
        bookingEntity.setStatusEntity(statusEntity);
        bookingEntity.setCreatedAt(LocalDateTime.now());
        for (Long idProduct : bookingRequest.getProductIds()) {
            BookingDetailEntity bookingDetailEntity = new BookingDetailEntity();
            ProductEntity productEntity = productRepository.findById(idProduct).get();
            RentalPriceEntity rentalPriceEntity = rentalPriceRepository.findByProductEntityAndRentalTypeEntity(productEntity, rentalTypeEntity);
            bookingDetailEntity.setBookingEntity(bookingEntity);
            bookingDetailEntity.setProductEntity(productEntity);
            bookingDetailEntity.setTotalPrice(rentalPriceEntity.getPrice());
            bookingDetailEntities.add(bookingDetailEntity);
        }
        bookingEntity.setBookingDetailEntities(bookingDetailEntities);

        StatusEntity statusDocument = statusRepository.findByStatusCode("NOT YET RECEIVED");
        documentsEntity.setBookingEntity(bookingEntity);
        documentsEntity.setStatusEntity(statusDocument);
        documentsEntity.setDocumentType(bookingRequest.getDocumentType());
        documentsEntity.setDocumentNumber(bookingRequest.getDocumentNumber());
        try {
            documentsEntity.setImageBack(bookingRequest.getImageBack().getBytes());
            documentsEntity.setImageFront(bookingRequest.getImageFront().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bookingEntity.setDocumentsEntity(documentsEntity);
        System.out.println("BOOKING ID = " + bookingEntity.getIdBooking());
        bookingRepository.save(bookingEntity);
        messageResponse.setMessage("Booking successful");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public Page<BookingDTO> getBookings(Integer page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<BookingEntity> bookingEntities = bookingRepository.findAll(pageable);
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        for (BookingEntity bookingEntity : bookingEntities) {
            BookingDTO bookingDTO = new BookingDTO();
            modelMapper.map(bookingEntity, bookingDTO);
            bookingDTO.setStatus(bookingEntity.getStatusEntity().getStatusCode());

            List<BookingDetailDTO> bookingDetailDTOS = new ArrayList<>();
            for (BookingDetailEntity bookingDetailEntity : bookingEntity.getBookingDetailEntities()) {
                BookingDetailDTO bookingDetailDTO = new BookingDetailDTO();
                modelMapper.map(bookingDetailEntity, bookingDetailDTO);

                ProductBookingDTO productBookingDTO = new ProductBookingDTO();
                modelMapper.map(bookingDetailEntity.getProductEntity(), productBookingDTO);
                productBookingDTO.setPrice(bookingDetailEntity.getTotalPrice());
                bookingDetailDTO.setProductBookingDTO(productBookingDTO);

                if (!bookingDetailEntity.getReturnDetailEntities().isEmpty()) {
                    for (ReturnDetailEntity returnDetailEntity : bookingDetailEntity.getReturnDetailEntities()) {
                        ReturnDetailDTO returnDetailDTO = new ReturnDetailDTO();
                        modelMapper.map(returnDetailEntity, returnDetailDTO);
                        bookingDetailDTO.setReturnDetailDTO(returnDetailDTO);
                    }
                }

                bookingDetailDTOS.add(bookingDetailDTO);
            }
            bookingDTO.setBookingDetailDTOS(bookingDetailDTOS);

            if (bookingEntity.getDepositTypeEntity() != null) {
                DepositTypeDTO depositTypeDTO = new DepositTypeDTO();
                modelMapper.map(bookingEntity.getDepositTypeEntity(), depositTypeDTO);
                bookingDTO.setDepositTypeDTO(depositTypeDTO);
            }


            UserBookingDTO userBookingDTO = new UserBookingDTO();
            modelMapper.map(bookingEntity.getUserEntity(), userBookingDTO);
            bookingDTO.setUserBookingDTO(userBookingDTO);

            DocumentBookingDTO documentBookingDTO = new DocumentBookingDTO();
            modelMapper.map(bookingEntity.getDocumentsEntity(), documentBookingDTO);
            documentBookingDTO.setStatus(bookingEntity.getDocumentsEntity().getStatusEntity().getStatusCode());
            documentBookingDTO.setImageFront(ConvertByteToBase64.toBase64(bookingEntity.getDocumentsEntity().getImageFront()));
            documentBookingDTO.setImageBack(ConvertByteToBase64.toBase64(bookingEntity.getDocumentsEntity().getImageBack()));
            bookingDTO.setDocumentBookingDTO(documentBookingDTO);

            List<PaymentBookingDTO> paymentBookingDTOS = new ArrayList<>();
            for (PaymentBookingEntity paymentBookingEntity : bookingEntity.getPaymentBookingEntities()) {
                PaymentBookingDTO paymentBookingDTO = new PaymentBookingDTO();
                modelMapper.map(paymentBookingEntity, paymentBookingDTO);
                paymentBookingDTO.setPaymentMethod(paymentBookingEntity.getPaymentsMethodEntity().getMethod());
                paymentBookingDTOS.add(paymentBookingDTO);
            }
            bookingDTO.setPaymentBookingDTOS(paymentBookingDTOS);

            bookingDTOS.add(bookingDTO);
        }
        return new PageImpl<>(bookingDTOS, bookingEntities.getPageable(), bookingEntities.getTotalElements());
    }

    @Override
    public MessageResponse acceptBooking(AcceptBookingRequest acceptBookingRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            BookingEntity bookingEntity = bookingRepository.findById(acceptBookingRequest.getBookingId()).get();
            try {
                StatusEntity statusEntity = statusRepository.findByStatusCode("ACCEPTED");
                try {
                    PaymentsMethodEntity paymentsMethodEntity = paymentsMethodRepository.findById(acceptBookingRequest.getPaymentMethodId()).get();
                    try {
                        DepositTypeEntity depositTypeEntity = depositTypeRepository.findById(acceptBookingRequest.getDepositTypeId()).get();
                        bookingEntity.setDepositTypeEntity(depositTypeEntity);
                        bookingEntity.setStatusEntity(statusEntity);

                        //luu tien coc cua khach
                        Double depositPrice = (depositTypeEntity.getPercentDeposit() * bookingEntity.getTotalAmount()) / 100;
                        PaymentBookingEntity paymentBookingEntity = new PaymentBookingEntity();
                        paymentBookingEntity.setPaymentsMethodEntity(paymentsMethodEntity);
                        paymentBookingEntity.setBookingEntity(bookingEntity);
                        paymentBookingEntity.setPaidAt(LocalDateTime.now());
                        paymentBookingEntity.setPrice(depositPrice);
                        paymentBookingEntity.setRemainAmount(bookingEntity.getTotalAmount() - depositPrice);
                        paymentBookingRepository.save(paymentBookingEntity);

                        bookingRepository.save(bookingEntity);

                        messageResponse.setMessage("Booking accepted");
                        messageResponse.setStatus(HttpStatus.OK);
                        return messageResponse;
                    } catch (NoSuchElementException ex) {
                        messageResponse.setMessage("Deposit type not found");
                        messageResponse.setStatus(HttpStatus.NOT_FOUND);
                        return messageResponse;
                    }
                } catch (NoSuchElementException ex) {
                    messageResponse.setMessage("Payment method not found");
                    messageResponse.setStatus(HttpStatus.NOT_FOUND);
                    return messageResponse;
                }
            } catch (NoSuchElementException ex) {
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                messageResponse.setMessage("Status not found");
                return messageResponse;
            }
        } catch (NoSuchElementException ex) {
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Booking not found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse updateStatusBooking(UpdateStatusBookingRequest updateStatusBookingRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            BookingEntity bookingEntity = bookingRepository.findById(updateStatusBookingRequest.getBookingId()).get();
            try {
                StatusEntity statusEntity = statusRepository.findById(updateStatusBookingRequest.getStatusId()).get();
                bookingEntity.setStatusEntity(statusEntity);
                if (statusEntity.getStatusCode().equals("RENTING")) {
                    for (BookingDetailEntity bookingDetailEntity : bookingEntity.getBookingDetailEntities()) {
                        StatusEntity statusProduct = statusRepository.findByStatusCode("RENTED");
                        bookingDetailEntity.getProductEntity().setStatusEntity(statusProduct);
                    }
                }
                bookingRepository.save(bookingEntity);
                messageResponse.setMessage("Booking updated");
                messageResponse.setStatus(HttpStatus.OK);
                return messageResponse;
            } catch (NoSuchElementException ex) {
                messageResponse.setMessage("Status not found");
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
        } catch (NoSuchElementException ex) {
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Booking not found");
            return messageResponse;
        }
    }

    @Override
    public Object getBookingsByUser(Long idUser) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        UserEntity userEntity = null;
        try{
            userEntity = userRepository.findById(idUser).get();
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("User not found");
            return messageResponse;
        }
        List<BookingEntity> bookingEntities = bookingRepository.findByUserEntity(userEntity);
        for (BookingEntity bookingEntity : bookingEntities) {
            BookingDTO bookingDTO = new BookingDTO();
            modelMapper.map(bookingEntity, bookingDTO);
            bookingDTO.setStatus(bookingEntity.getStatusEntity().getStatusCode());

            List<BookingDetailDTO> bookingDetailDTOS = new ArrayList<>();
            for (BookingDetailEntity bookingDetailEntity : bookingEntity.getBookingDetailEntities()) {
                BookingDetailDTO bookingDetailDTO = new BookingDetailDTO();
                modelMapper.map(bookingDetailEntity, bookingDetailDTO);

                ProductBookingDTO productBookingDTO = new ProductBookingDTO();
                modelMapper.map(bookingDetailEntity.getProductEntity(), productBookingDTO);
                productBookingDTO.setPrice(bookingDetailEntity.getTotalPrice());
                bookingDetailDTO.setProductBookingDTO(productBookingDTO);

                if (!bookingDetailEntity.getReturnDetailEntities().isEmpty()) {
                    for (ReturnDetailEntity returnDetailEntity : bookingDetailEntity.getReturnDetailEntities()) {
                        ReturnDetailDTO returnDetailDTO = new ReturnDetailDTO();
                        modelMapper.map(returnDetailEntity, returnDetailDTO);
                        bookingDetailDTO.setReturnDetailDTO(returnDetailDTO);
                    }
                }

                bookingDetailDTOS.add(bookingDetailDTO);
            }
            bookingDTO.setBookingDetailDTOS(bookingDetailDTOS);

            if (bookingEntity.getDepositTypeEntity() != null) {
                DepositTypeDTO depositTypeDTO = new DepositTypeDTO();
                modelMapper.map(bookingEntity.getDepositTypeEntity(), depositTypeDTO);
                bookingDTO.setDepositTypeDTO(depositTypeDTO);
            }


            UserBookingDTO userBookingDTO = new UserBookingDTO();
            modelMapper.map(bookingEntity.getUserEntity(), userBookingDTO);
            bookingDTO.setUserBookingDTO(userBookingDTO);

            DocumentBookingDTO documentBookingDTO = new DocumentBookingDTO();
            modelMapper.map(bookingEntity.getDocumentsEntity(), documentBookingDTO);
            documentBookingDTO.setStatus(bookingEntity.getDocumentsEntity().getStatusEntity().getStatusCode());
            documentBookingDTO.setImageFront(ConvertByteToBase64.toBase64(bookingEntity.getDocumentsEntity().getImageFront()));
            documentBookingDTO.setImageBack(ConvertByteToBase64.toBase64(bookingEntity.getDocumentsEntity().getImageBack()));
            bookingDTO.setDocumentBookingDTO(documentBookingDTO);

            List<PaymentBookingDTO> paymentBookingDTOS = new ArrayList<>();
            for (PaymentBookingEntity paymentBookingEntity : bookingEntity.getPaymentBookingEntities()) {
                PaymentBookingDTO paymentBookingDTO = new PaymentBookingDTO();
                modelMapper.map(paymentBookingEntity, paymentBookingDTO);
                paymentBookingDTO.setPaymentMethod(paymentBookingEntity.getPaymentsMethodEntity().getMethod());
                paymentBookingDTOS.add(paymentBookingDTO);
            }
            bookingDTO.setPaymentBookingDTOS(paymentBookingDTOS);

            bookingDTOS.add(bookingDTO);
        }
        dataResponse.setStatus(HttpStatus.OK);
        dataResponse.setData(bookingDTOS);
        dataResponse.setMessage("Success");
        return dataResponse;
    }

    @Override
    public Object getBookingById(Long idBooking) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        BookingEntity bookingEntity = null;
        try {
            bookingEntity = bookingRepository.findById(idBooking).get();
            BookingDTO bookingDTO = new BookingDTO();
            modelMapper.map(bookingEntity, bookingDTO);
            bookingDTO.setStatus(bookingEntity.getStatusEntity().getStatusCode());

            List<BookingDetailDTO> bookingDetailDTOS = new ArrayList<>();
            for (BookingDetailEntity bookingDetailEntity : bookingEntity.getBookingDetailEntities()) {
                BookingDetailDTO bookingDetailDTO = new BookingDetailDTO();
                modelMapper.map(bookingDetailEntity, bookingDetailDTO);

                ProductBookingDTO productBookingDTO = new ProductBookingDTO();
                modelMapper.map(bookingDetailEntity.getProductEntity(), productBookingDTO);
                productBookingDTO.setPrice(bookingDetailEntity.getTotalPrice());
                bookingDetailDTO.setProductBookingDTO(productBookingDTO);

                if (!bookingDetailEntity.getReturnDetailEntities().isEmpty()) {
                    for (ReturnDetailEntity returnDetailEntity : bookingDetailEntity.getReturnDetailEntities()) {
                        ReturnDetailDTO returnDetailDTO = new ReturnDetailDTO();
                        modelMapper.map(returnDetailEntity, returnDetailDTO);
                        bookingDetailDTO.setReturnDetailDTO(returnDetailDTO);
                    }
                }

                bookingDetailDTOS.add(bookingDetailDTO);
            }
            bookingDTO.setBookingDetailDTOS(bookingDetailDTOS);

            if (bookingEntity.getDepositTypeEntity() != null) {
                DepositTypeDTO depositTypeDTO = new DepositTypeDTO();
                modelMapper.map(bookingEntity.getDepositTypeEntity(), depositTypeDTO);
                bookingDTO.setDepositTypeDTO(depositTypeDTO);
            }


            UserBookingDTO userBookingDTO = new UserBookingDTO();
            modelMapper.map(bookingEntity.getUserEntity(), userBookingDTO);
            bookingDTO.setUserBookingDTO(userBookingDTO);

            DocumentBookingDTO documentBookingDTO = new DocumentBookingDTO();
            modelMapper.map(bookingEntity.getDocumentsEntity(), documentBookingDTO);
            documentBookingDTO.setStatus(bookingEntity.getDocumentsEntity().getStatusEntity().getStatusCode());
            documentBookingDTO.setImageFront(ConvertByteToBase64.toBase64(bookingEntity.getDocumentsEntity().getImageFront()));
            documentBookingDTO.setImageBack(ConvertByteToBase64.toBase64(bookingEntity.getDocumentsEntity().getImageBack()));
            bookingDTO.setDocumentBookingDTO(documentBookingDTO);

            List<PaymentBookingDTO> paymentBookingDTOS = new ArrayList<>();
            for (PaymentBookingEntity paymentBookingEntity : bookingEntity.getPaymentBookingEntities()) {
                PaymentBookingDTO paymentBookingDTO = new PaymentBookingDTO();
                modelMapper.map(paymentBookingEntity, paymentBookingDTO);
                paymentBookingDTO.setPaymentMethod(paymentBookingEntity.getPaymentsMethodEntity().getMethod());
                paymentBookingDTOS.add(paymentBookingDTO);
            }
            bookingDTO.setPaymentBookingDTOS(paymentBookingDTOS);

            dataResponse.setStatus(HttpStatus.OK);
            dataResponse.setData(bookingDTO);
            dataResponse.setMessage("Success");
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Booking not found");
            return messageResponse;
        }
    }
}
