package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.DepositTypeEntity;
import org.example.yci_web.Model.DTO.DepositTypeDTO;
import org.example.yci_web.Model.Request.DepositTypeRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.DepositTypeRepository;
import org.example.yci_web.Service.DepositTypeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DepositTypeServiceImpl implements DepositTypeService {
    @Autowired
    DepositTypeRepository depositTypeRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public DataResponse getAllDepositTypes() {
        DataResponse dataResponse = new DataResponse();
        List<DepositTypeDTO> depositTypeDTOS = new ArrayList<>();
        List<DepositTypeEntity> depositTypeEntities = depositTypeRepository.findAll();
        for (DepositTypeEntity depositTypeEntity : depositTypeEntities) {
            DepositTypeDTO depositTypeDTO = new DepositTypeDTO();
            modelMapper.map(depositTypeEntity, depositTypeDTO);
            depositTypeDTOS.add(depositTypeDTO);
        }
        dataResponse.setData(depositTypeDTOS);
        dataResponse.setMessage("Success");
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getDepositTypeById(Long id) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        DepositTypeDTO depositTypeDTO = new DepositTypeDTO();
        try{
            DepositTypeEntity depositTypeEntity = depositTypeRepository.findById(id).get();
            modelMapper.map(depositTypeEntity, depositTypeDTO);
            dataResponse.setMessage("Success");
            dataResponse.setStatus(HttpStatus.OK);
            dataResponse.setData(depositTypeDTO);
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found deposit type");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse addDepositType(DepositTypeRequest depositTypeRequest) {
        MessageResponse messageResponse = new MessageResponse();
        DepositTypeEntity depositTypeEntity = new DepositTypeEntity();
        modelMapper.map(depositTypeRequest, depositTypeEntity);
        depositTypeRepository.save(depositTypeEntity);
        messageResponse.setMessage("Success");
        messageResponse.setStatus(HttpStatus.OK);
        return messageResponse;
    }

    @Override
    public MessageResponse updateDepositType(DepositTypeRequest depositTypeRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            DepositTypeEntity depositTypeEntity = depositTypeRepository.findById(depositTypeRequest.getIdDepositType()).get();
            modelMapper.map(depositTypeRequest, depositTypeEntity);
            depositTypeRepository.save(depositTypeEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found deposit type");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse deleteDepositType(Long id) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            DepositTypeEntity depositTypeEntity = depositTypeRepository.findById(id).get();
            depositTypeRepository.delete(depositTypeEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("Can not found deposit type");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }
}
