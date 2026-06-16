package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.DocumentsEntity;
import org.example.yci_web.Entity.StatusEntity;
import org.example.yci_web.Model.DTO.DocumentDTO;
import org.example.yci_web.Model.Request.UpdateStatusDocumentRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.DocumentsRepository;
import org.example.yci_web.Repository.StatusRepository;
import org.example.yci_web.Service.DocumentService;
import org.example.yci_web.Utils.ConvertByteToBase64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DocumentServiceImpl implements DocumentService {
    @Autowired
    DocumentsRepository documentsRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    StatusRepository statusRepository;


    @Override
    public Page<DocumentDTO> getDocuments(Integer page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<DocumentsEntity> documentsEntities = documentsRepository.findAll(pageable);
        List<DocumentDTO> documentDTOS = new ArrayList<>();
        for (DocumentsEntity documentsEntity : documentsEntities) {
            DocumentDTO documentDTO = new DocumentDTO();
            modelMapper.map(documentsEntity, documentDTO);
            documentDTO.setImageFront(ConvertByteToBase64.toBase64(documentsEntity.getImageFront()));
            documentDTO.setImageBack(ConvertByteToBase64.toBase64(documentsEntity.getImageBack()));
            documentDTO.setStatus(documentsEntity.getStatusEntity().getStatusCode());
            documentDTO.setIdBooking(documentsEntity.getBookingEntity().getIdBooking());
            documentDTO.setStatusBooking(documentsEntity.getBookingEntity().getStatusEntity().getStatusCode());
            documentDTO.setTimeStart(documentsEntity.getBookingEntity().getTimeStart());
            documentDTO.setTimeEnd(documentsEntity.getBookingEntity().getTimeEnd());
            documentDTO.setIdUser(documentsEntity.getBookingEntity().getUserEntity().getIdUser());
            documentDTO.setFullName(documentsEntity.getBookingEntity().getUserEntity().getFullName());
            documentDTO.setPhone(documentsEntity.getBookingEntity().getUserEntity().getPhone());
            documentDTOS.add(documentDTO);
        }
        return new PageImpl<>(documentDTOS, documentsEntities.getPageable(), documentsEntities.getTotalElements());
    }

    @Override
    public Object getDocumentById(Long idDocument) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try {
            DocumentsEntity documentsEntity = documentsRepository.findById(idDocument).get();

            DocumentDTO documentDTO = new DocumentDTO();
            modelMapper.map(documentsEntity, documentDTO);
            documentDTO.setImageFront(ConvertByteToBase64.toBase64(documentsEntity.getImageFront()));
            documentDTO.setImageBack(ConvertByteToBase64.toBase64(documentsEntity.getImageBack()));
            documentDTO.setStatus(documentsEntity.getStatusEntity().getStatusCode());
            documentDTO.setIdBooking(documentsEntity.getBookingEntity().getIdBooking());
            documentDTO.setStatusBooking(documentsEntity.getBookingEntity().getStatusEntity().getStatusCode());
            documentDTO.setTimeStart(documentsEntity.getBookingEntity().getTimeStart());
            documentDTO.setTimeEnd(documentsEntity.getBookingEntity().getTimeEnd());
            documentDTO.setIdUser(documentsEntity.getBookingEntity().getUserEntity().getIdUser());
            documentDTO.setFullName(documentsEntity.getBookingEntity().getUserEntity().getFullName());
            documentDTO.setPhone(documentsEntity.getBookingEntity().getUserEntity().getPhone());

            dataResponse.setData(documentDTO);
            dataResponse.setMessage("Success");
            dataResponse.setStatus(HttpStatus.OK);

            return dataResponse;

        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found document");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse updateStatusDoccument(UpdateStatusDocumentRequest updateStatusDocumentRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            DocumentsEntity documentsEntity = documentsRepository.findById(updateStatusDocumentRequest.getDocumentId()).get();
            try {
                StatusEntity statusEntity = statusRepository.findById(updateStatusDocumentRequest.getStatusId()).get();
                documentsEntity.setStatusEntity(statusEntity);
                documentsRepository.save(documentsEntity);

                messageResponse.setMessage("Success");
                messageResponse.setStatus(HttpStatus.OK);
                return messageResponse;
            }catch (NoSuchElementException ex){
                messageResponse.setMessage("Can not found status");
                messageResponse.setStatus(HttpStatus.NOT_FOUND);
                return messageResponse;
            }
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found document");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse deleteDocumentById(Long idDocument) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            DocumentsEntity documentsEntity = documentsRepository.findById(idDocument).get();
            documentsRepository.delete(documentsEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found document");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }
}
