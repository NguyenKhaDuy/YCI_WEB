package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.RentalRegulationsEntity;
import org.example.yci_web.Entity.ScheduleKeepingRegulationsEntity;
import org.example.yci_web.Model.DTO.RentalRegulationsDTO;
import org.example.yci_web.Model.DTO.ScheduleKeepingRegulationsDTO;
import org.example.yci_web.Model.Request.ScheduleKeepingRegulationsRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Repository.ScheduleKeepingRegulationsRepository;
import org.example.yci_web.Service.ScheduleKeepingRegulationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ScheduleKeepingRegulationsServiceImpl implements ScheduleKeepingRegulationService {
    @Autowired
    ScheduleKeepingRegulationsRepository scheduleKeepingRegulationsRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public DataResponse getScheduleRegulations() {
        List<ScheduleKeepingRegulationsEntity> scheduleKeepingRegulationsEntities = scheduleKeepingRegulationsRepository.findAll();
        List<ScheduleKeepingRegulationsDTO> scheduleKeepingRegulationsDTOS = new ArrayList<>();
        DataResponse dataResponse = new DataResponse();
        for (ScheduleKeepingRegulationsEntity scheduleKeepingRegulationsEntity : scheduleKeepingRegulationsEntities) {
            ScheduleKeepingRegulationsDTO scheduleKeepingRegulationsDTO = new ScheduleKeepingRegulationsDTO();
            modelMapper.map(scheduleKeepingRegulationsEntity, scheduleKeepingRegulationsDTO);
            scheduleKeepingRegulationsDTOS.add(scheduleKeepingRegulationsDTO);
        }
        dataResponse.setMessage("Success");
        dataResponse.setData(scheduleKeepingRegulationsDTOS);
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }

    @Override
    public Object getScheduleRegulationsById(Long idScheduleRegulations) {
        MessageResponse messageResponse = new MessageResponse();
        DataResponse dataResponse = new DataResponse();
        try{
            ScheduleKeepingRegulationsEntity scheduleKeepingRegulationsEntity = scheduleKeepingRegulationsRepository.findById(idScheduleRegulations).get();
            ScheduleKeepingRegulationsDTO scheduleKeepingRegulationsDTO = new ScheduleKeepingRegulationsDTO();
            modelMapper.map(scheduleKeepingRegulationsEntity, scheduleKeepingRegulationsDTO);
            dataResponse.setMessage("Success");
            dataResponse.setData(scheduleKeepingRegulationsDTO);
            dataResponse.setStatus(HttpStatus.OK);
            return dataResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Schedule keeping regulation Not Found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse addScheduleRegulations(ScheduleKeepingRegulationsRequest scheduleKeepingRegulationsRequest) {
        MessageResponse messageResponse = new MessageResponse();
        ScheduleKeepingRegulationsEntity scheduleKeepingRegulationsEntity = new ScheduleKeepingRegulationsEntity();
        modelMapper.map(scheduleKeepingRegulationsRequest, scheduleKeepingRegulationsEntity);
        scheduleKeepingRegulationsEntity.setUpdatedAt(LocalDateTime.now());
        scheduleKeepingRegulationsEntity.setCreatedAt(LocalDateTime.now());
        scheduleKeepingRegulationsRepository.save(scheduleKeepingRegulationsEntity);
        messageResponse.setStatus(HttpStatus.OK);
        messageResponse.setMessage("Success");
        return messageResponse;
    }

    @Override
    public MessageResponse deleteScheduleRegulations(Long idScheduleRegulations) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            ScheduleKeepingRegulationsEntity scheduleKeepingRegulationsEntity = scheduleKeepingRegulationsRepository.findById(idScheduleRegulations).get();
            scheduleKeepingRegulationsRepository.delete(scheduleKeepingRegulationsEntity);
            messageResponse.setStatus(HttpStatus.OK);
            messageResponse.setMessage("Success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Schedule keeping regulation Not Found");
            return messageResponse;
        }
    }

    @Override
    public MessageResponse updateScheduleRegulations(ScheduleKeepingRegulationsRequest scheduleKeepingRegulationsRequest) {
        MessageResponse messageResponse = new MessageResponse();
        try {
            ScheduleKeepingRegulationsEntity scheduleKeepingRegulationsEntity = scheduleKeepingRegulationsRepository.findById(scheduleKeepingRegulationsRequest.getIdScheduleKeepingRegulations()).get();
            modelMapper.map(scheduleKeepingRegulationsRequest, scheduleKeepingRegulationsEntity);
            scheduleKeepingRegulationsEntity.setUpdatedAt(LocalDateTime.now());
            scheduleKeepingRegulationsRepository.save(scheduleKeepingRegulationsEntity);
            messageResponse.setStatus(HttpStatus.OK);
            messageResponse.setMessage("Success");
            return messageResponse;
        }catch (NoSuchElementException ex){
            messageResponse.setStatus(HttpStatus.NOT_FOUND);
            messageResponse.setMessage("Schedule keeping regulation Not Found");
            return messageResponse;
        }
    }
}
