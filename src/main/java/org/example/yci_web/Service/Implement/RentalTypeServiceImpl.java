package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.RentalTypeEntity;
import org.example.yci_web.Model.DTO.RentalTypeDTO;
import org.example.yci_web.Model.Request.RentalTypeRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.RentalTypeRepository;
import org.example.yci_web.Service.RentalTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RentalTypeServiceImpl implements RentalTypeService {
    @Autowired
    RentalTypeRepository rentalTypeRepository;
    @Autowired
    ModelMapper modelMapper;


    @Override
    public DataResponse getAllRentalTypes() {
        List<RentalTypeEntity> rentalTypeEntities = rentalTypeRepository.findAll();
        List<RentalTypeDTO> rentalTypeDTOs = new ArrayList<>();
        DataResponse dataResponse = new DataResponse();
        for (RentalTypeEntity rentalTypeEntity : rentalTypeEntities) {
            RentalTypeDTO rentalTypeDTO = new RentalTypeDTO();
            modelMapper.map(rentalTypeEntity, rentalTypeDTO);
            rentalTypeDTOs.add(rentalTypeDTO);
        }
        dataResponse.setMessage("Success");
        dataResponse.setData(rentalTypeDTOs);
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getRentalTypeById(Long idType) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try{
            RentalTypeEntity rentalTypeEntity = rentalTypeRepository.findById(idType).get();
            RentalTypeDTO rentalTypeDTO = new RentalTypeDTO();
            modelMapper.map(rentalTypeEntity, rentalTypeDTO);
            dataResponse.setMessage("Success");
            dataResponse.setData(rentalTypeDTO);
            dataResponse.setStatus(HttpStatus.OK);
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Rental Type Not Found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse addRentalType(RentalTypeRequest rentalTypeRequest) {
        MessageResponse messageResponse = new MessageResponse();
        RentalTypeEntity rentalTypeEntity = new RentalTypeEntity();
        modelMapper.map(rentalTypeRequest, rentalTypeEntity);
        rentalTypeRepository.save(rentalTypeEntity);
        messageResponse.setStatus(HttpStatus.OK);
        messageResponse.setMessage("Success");
        return messageResponse;
    }

    @Override
    public MessageResponse updateRentalType(RentalTypeRequest rentalTypeRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            RentalTypeEntity rentalTypeEntity = rentalTypeRepository.findById(rentalTypeRequest.getIdRentalType()).get();
            modelMapper.map(rentalTypeRequest, rentalTypeEntity);
            rentalTypeRepository.save(rentalTypeEntity);
            messageResponse.setStatus(HttpStatus.OK);
            messageResponse.setMessage("Success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Rental Type Not Found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse deleteRentalType(Long idType) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            RentalTypeEntity rentalTypeEntity = rentalTypeRepository.findById(idType).get();
            rentalTypeRepository.delete(rentalTypeEntity);
            messageResponse.setStatus(HttpStatus.OK);
            messageResponse.setMessage("Success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Rental Type Not Found");
            return messageResponse;
        }
    }
}
