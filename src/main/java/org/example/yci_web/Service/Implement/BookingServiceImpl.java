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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        double totalAmount = 0;

        if (bookingRequest.getTimeStart() == null
                || bookingRequest.getTimeEnd() == null
                || !bookingRequest.getTimeEnd().isAfter(bookingRequest.getTimeStart())) {
            messageResponse.setMessage("Thời gian thuê không hợp lệ");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        if (bookingRequest.getProductIds() == null || bookingRequest.getProductIds().isEmpty()) {
            messageResponse.setMessage("Vui lòng chọn ít nhất một thiết bị");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        if (bookingRequest.getImageFront() == null
                || bookingRequest.getImageFront().isEmpty()
                || bookingRequest.getImageBack() == null
                || bookingRequest.getImageBack().isEmpty()) {
            messageResponse.setMessage("Vui lòng tải ảnh giấy tờ thế chấp");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }

        try {
            userEntity = userRepository.findById(bookingRequest.getUserId()).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy khách hàng");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        if (userEntity.getRole() != null && userEntity.getRole() == 0) {
            messageResponse.setMessage("Tài khoản quản trị chỉ được xem, không thể đặt thuê");
            messageResponse.setStatus(HttpStatus.FORBIDDEN);
            return messageResponse;
        }
        statusEntity = findOrCreateStatus("WAITING ACCEPT");
        try {
            rentalTypeEntity = rentalTypeRepository.findById(bookingRequest.getIdRentalType()).get();
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy loại giá thuê");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
        double rentalHours = rentalDurationHours(rentalTypeEntity.getType());
        if (rentalHours <= 0) {
            messageResponse.setMessage("Loại giá thuê chưa cấu hình thời lượng rõ ràng");
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        long expectedMinutes = Math.round(rentalHours * 60);
        long actualMinutes = Duration.between(bookingRequest.getTimeStart(), bookingRequest.getTimeEnd()).toMinutes();
        if (actualMinutes != expectedMinutes) {
            messageResponse.setMessage("Thời gian thuê phải khớp với gói " + rentalTypeEntity.getType());
            messageResponse.setStatus(HttpStatus.BAD_REQUEST);
            return messageResponse;
        }
        Set<Long> requestedProductIds = new LinkedHashSet<>(bookingRequest.getProductIds());
        for (BookingEntity booking : bookingRepository.findAll()) {
            if (isClosedStatus(booking.getStatusEntity())) {
                continue;
            }
            boolean overlap = bookingRequest.getTimeStart().isBefore(booking.getTimeEnd())
                    && bookingRequest.getTimeEnd().isAfter(booking.getTimeStart());
            if (!overlap) {
                continue;
            }
            for (BookingDetailEntity detail : booking.getBookingDetailEntities()) {
                if (detail.getProductEntity() != null
                        && requestedProductIds.contains(detail.getProductEntity().getIdProduct())) {
                    messageResponse.setMessage("Thiết bị " + detail.getProductEntity().getNameProduct() + " đã có lịch thuê trong khoảng thời gian này");
                    messageResponse.setStatus(HttpStatus.BAD_REQUEST);
                    return messageResponse;
                }
            }
        }

//        modelMapper.map(bookingRequest, bookingEntity);
        bookingEntity.setTimeStart(bookingRequest.getTimeStart());
        bookingEntity.setTimeEnd(bookingRequest.getTimeEnd());
        bookingEntity.setNote(bookingRequest.getNote());
        bookingEntity.setUserEntity(userEntity);
        bookingEntity.setStatusEntity(statusEntity);
        bookingEntity.setCreatedAt(LocalDateTime.now());
        for (Long idProduct : requestedProductIds) {
            BookingDetailEntity bookingDetailEntity = new BookingDetailEntity();
            ProductEntity productEntity;
            try {
                productEntity = productRepository.findById(idProduct).get();
            } catch (NoSuchElementException ex) {
                messageResponse.setMessage("Không tìm thấy thiết bị");
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
            RentalPriceEntity rentalPriceEntity = rentalPriceRepository.findByProductEntityAndRentalTypeEntity(productEntity, rentalTypeEntity);
            if (rentalPriceEntity == null) {
                messageResponse.setMessage("Thiết bị " + productEntity.getNameProduct() + " chưa có giá thuê cho loại giá đã chọn");
                messageResponse.setStatus(HttpStatus.BAD_REQUEST);
                return messageResponse;
            }
            bookingDetailEntity.setBookingEntity(bookingEntity);
            bookingDetailEntity.setProductEntity(productEntity);
            bookingDetailEntity.setTotalPrice(rentalPriceEntity.getPrice());
            totalAmount += rentalPriceEntity.getPrice() == null ? 0 : rentalPriceEntity.getPrice();
            bookingDetailEntities.add(bookingDetailEntity);
        }
        bookingEntity.setTotalAmount(totalAmount);
        bookingEntity.setBookingDetailEntities(bookingDetailEntities);

        StatusEntity statusDocument = findOrCreateStatus("NOT YET RECEIVED");
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
        bookingRepository.save(bookingEntity);
        messageResponse.setMessage("Đặt lịch thuê thành công");
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

                        messageResponse.setMessage("Xác nhận đơn thuê thành công");
                        messageResponse.setStatus(HttpStatus.OK);
                        return messageResponse;
                    } catch (NoSuchElementException ex) {
                        messageResponse.setMessage("Không tìm thấy loại tiền cọc");
                        messageResponse.setStatus(HttpStatus.NOT_FOUND);
                        return messageResponse;
                    }
                } catch (NoSuchElementException ex) {
                    messageResponse.setMessage("Không tìm thấy phương thức thanh toán");
                    messageResponse.setStatus(HttpStatus.NOT_FOUND);
                    return messageResponse;
                }
            } catch (NoSuchElementException ex) {
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                messageResponse.setMessage("Không tìm thấy trạng thái đơn thuê");
                return messageResponse;
            }
        } catch (NoSuchElementException ex) {
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Không tìm thấy đơn thuê");
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
                messageResponse.setMessage("Cập nhật trạng thái đơn thuê thành công");
                messageResponse.setStatus(HttpStatus.OK);
                return messageResponse;
            } catch (NoSuchElementException ex) {
                messageResponse.setMessage("Không tìm thấy trạng thái đơn thuê");
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
        } catch (NoSuchElementException ex) {
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Không tìm thấy đơn thuê");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse cancelBooking(Long idBooking, Long idUser) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            BookingEntity bookingEntity = bookingRepository.findById(idBooking).get();
            if (idUser != null
                    && bookingEntity.getUserEntity() != null
                    && !bookingEntity.getUserEntity().getIdUser().equals(idUser)) {
                messageResponse.setMessage("Bạn không có quyền hủy đơn thuê này");
                messageResponse.setStatus(HttpStatus.FORBIDDEN);
                return messageResponse;
            }
            String statusCode = bookingEntity.getStatusEntity() == null ? "" : bookingEntity.getStatusEntity().getStatusCode();
            if (!isWaitingStatus(statusCode)) {
                messageResponse.setMessage("Chỉ được hủy đơn khi đơn còn chờ xác nhận");
                messageResponse.setStatus(HttpStatus.BAD_REQUEST);
                return messageResponse;
            }
            bookingEntity.setStatusEntity(findOrCreateStatus("CANCELLED"));
            bookingRepository.save(bookingEntity);
            messageResponse.setMessage("Đã hủy đơn thuê");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        } catch (NoSuchElementException ex) {
            messageResponse.setMessage("Không tìm thấy đơn thuê");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
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
            messageResponse.setMessage("Không tìm thấy khách hàng");
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
        dataResponse.setMessage("Thành công");
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
            dataResponse.setMessage("Thành công");
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Không tìm thấy đơn thuê");
            return messageResponse;
        }
    }

    private StatusEntity findOrCreateStatus(String statusCode) {
        StatusEntity statusEntity = statusRepository.findByStatusCode(statusCode);
        if (statusEntity != null) {
            return statusEntity;
        }
        statusEntity = new StatusEntity();
        statusEntity.setStatusCode(statusCode);
        return statusRepository.save(statusEntity);
    }

    private boolean isClosedStatus(StatusEntity statusEntity) {
        String statusCode = statusEntity == null ? "" : statusEntity.getStatusCode();
        String normalized = statusCode == null ? "" : statusCode.trim().toUpperCase();
        return normalized.contains("CANCEL") || normalized.contains("RETURNED") || normalized.contains("COMPLETED");
    }

    private boolean isWaitingStatus(String statusCode) {
        String normalized = statusCode == null ? "" : statusCode.trim().toUpperCase();
        return normalized.equals("WAITING ACCEPT")
                || normalized.equals("WAITING_ACCEPT")
                || normalized.equals("PENDING")
                || normalized.equals("CHỜ XÁC NHẬN");
    }

    private double rentalDurationHours(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT).replace(",", ".");
        Matcher hourMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(H|GIỜ|GIO|HOUR)").matcher(normalized);
        if (hourMatcher.find()) {
            return Double.parseDouble(hourMatcher.group(1));
        }
        Matcher dayMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(D|DAY|NGÀY|NGAY)").matcher(normalized);
        if (dayMatcher.find()) {
            return Double.parseDouble(dayMatcher.group(1)) * 24;
        }
        return 0;
    }
}
