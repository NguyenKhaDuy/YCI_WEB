package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.StatusEntity;
import org.example.yci_web.Model.DTO.StatusDTO;
import org.example.yci_web.Model.Request.StatusRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.StatusRepository;
import org.example.yci_web.Service.StatusService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StatusServiceImpl implements StatusService {
    @Autowired
    StatusRepository statusRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public DataResponse getAllStatus() {
        DataResponse dataResponse = new DataResponse();
        List<StatusEntity> statusList = statusRepository.findAll();
        List<StatusDTO> statusDTOList = new ArrayList<>();
        for (StatusEntity statusEntity : statusList) {
            StatusDTO statusDTO = new StatusDTO();
            modelMapper.map(statusEntity, statusDTO);
            statusDTOList.add(statusDTO);
        }
        dataResponse.setData(statusDTOList);
        dataResponse.setMessage("Success");
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getStatusById(Long idStatus) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try{
            StatusEntity statusEntity = statusRepository.findById(idStatus).get();
            StatusDTO statusDTO = new StatusDTO();
            modelMapper.map(statusEntity, statusDTO);
            dataResponse.setMessage("Success");
            dataResponse.setStatus(HttpStatus.OK);
            dataResponse.setData(statusDTO);
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("No such status");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            return messageResponse;
        }
    }

    @Override
    public MessageResponse addStatus(StatusRequest statusRequest) {
        MessageResponse messageResponse = new MessageResponse();
        StatusEntity statusEntity = new StatusEntity();
        modelMapper.map(statusRequest, statusEntity);
        statusRepository.save(statusEntity);
        return messageResponse;
    }

    @Override
    public MessageResponse updateStatus(StatusRequest statusRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            StatusEntity statusEntity = statusRepository.findById(statusRequest.getIdStatus()).get();
            modelMapper.map(statusRequest, statusEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("No such status");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return messageResponse;
    }

    @Override
    public MessageResponse deleteStatus(Long idStatus) {
        MessageResponse messageResponse = new MessageResponse();
        try{
            StatusEntity statusEntity = statusRepository.findById(idStatus).get();
            statusRepository.delete(statusEntity);
            messageResponse.setMessage("Success");
            messageResponse.setStatus(HttpStatus.OK);
        }catch (NoSuchElementException ex){
            messageResponse.setMessage("No such status");
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return messageResponse;
    }
}
