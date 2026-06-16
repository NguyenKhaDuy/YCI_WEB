package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.RentalRegulationsEntity;
import org.example.yci_web.Entity.RentalTypeEntity;
import org.example.yci_web.Model.DTO.RentalRegulationsDTO;
import org.example.yci_web.Model.DTO.RentalTypeDTO;
import org.example.yci_web.Model.Request.RentalRegulationsRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.RentalRegulationsRepository;
import org.example.yci_web.Service.RentalRegulationsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RentalRegulationsSercviceImpl implements RentalRegulationsService {
    @Autowired
    RentalRegulationsRepository rentalRegulationsRepository;
    @Autowired
    ModelMapper modelMapper;


    @Override
    public DataResponse getRentalRegulations() {
        List<RentalRegulationsEntity> rentalRegulationsEntities = rentalRegulationsRepository.findAll();
        List<RentalRegulationsDTO> rentalRegulationsDTOS = new ArrayList<>();
        DataResponse dataResponse = new DataResponse();
        for (RentalRegulationsEntity rentalRegulationsEntity : rentalRegulationsEntities) {
            RentalRegulationsDTO rentalRegulationsDTO = new RentalRegulationsDTO();
            modelMapper.map(rentalRegulationsEntity, rentalRegulationsDTO);
            rentalRegulationsDTOS.add(rentalRegulationsDTO);
        }
        dataResponse.setMessage("Success");
        dataResponse.setData(rentalRegulationsDTOS);
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getRentalRegulationsById(Long idRentalRegulations) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try{
            RentalRegulationsEntity rentalRegulationsEntity = rentalRegulationsRepository.findById(idRentalRegulations).get();
            RentalRegulationsDTO rentalRegulationsDTO = new RentalRegulationsDTO();
            modelMapper.map(rentalRegulationsEntity, rentalRegulationsDTO);
            dataResponse.setMessage("Success");
            dataResponse.setData(rentalRegulationsDTO);
            dataResponse.setStatus(HttpStatus.OK);
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Rental regulation Not Found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse addRentalRegulations(RentalRegulationsRequest rentalRegulationsRequest) {
        MessageResponse messageResponse = new MessageResponse();
        RentalRegulationsEntity rentalRegulationsEntity = new RentalRegulationsEntity();
        modelMapper.map(rentalRegulationsRequest, rentalRegulationsEntity);
        rentalRegulationsEntity.setUpdatedAt(LocalDateTime.now());
        rentalRegulationsEntity.setCreatedAt(LocalDateTime.now());
        rentalRegulationsRepository.save(rentalRegulationsEntity);
        messageResponse.setStatus(HttpStatus.OK);
        messageResponse.setMessage("Success");
        return messageResponse;
    }

    @Override
    public MessageResponse deleteRentalRegulations(Long idRentalRegulations) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            RentalRegulationsEntity rentalRegulationsEntity = rentalRegulationsRepository.findById(idRentalRegulations).get();
            rentalRegulationsRepository.delete(rentalRegulationsEntity);
            messageResponse.setStatus(HttpStatus.OK);
            messageResponse.setMessage("Success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Rental regulation Not Found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse updateRentalRegulations(RentalRegulationsRequest rentalRegulationsRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            RentalRegulationsEntity rentalRegulationsEntity = rentalRegulationsRepository.findById(rentalRegulationsRequest.getIdRentalRegulations()).get();
            modelMapper.map(rentalRegulationsRequest, rentalRegulationsEntity);
            rentalRegulationsEntity.setUpdatedAt(LocalDateTime.now());
            rentalRegulationsRepository.save(rentalRegulationsEntity);
            messageResponse.setStatus(HttpStatus.OK);
            messageResponse.setMessage("Success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Rental regulation Not Found");
            return messageResponse;
        }
    }
}
