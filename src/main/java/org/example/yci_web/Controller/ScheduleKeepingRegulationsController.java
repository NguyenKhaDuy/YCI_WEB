package org.example.yci_web.Controller;

import org.example.yci_web.Model.Request.RentalRegulationsRequest;
import org.example.yci_web.Model.Request.ScheduleKeepingRegulationsRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;
import org.example.yci_web.Service.ScheduleKeepingRegulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScheduleKeepingRegulationsController {
    @Autowired
    ScheduleKeepingRegulationService scheduleKeepingRegulationService;

    @GetMapping(value = "/api/admin/schedule-keeping-regulation")
    public ResponseEntity<Object> getAllScheduleKeepingRegulation(){
        DataResponse dataResponse = scheduleKeepingRegulationService.getScheduleRegulations();
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/api/admin/schedule-keeping-regulation/id={id}")
    public ResponseEntity<Object> getScheduleKeepingRegulationById(@PathVariable("id") Long id){
        Object result = scheduleKeepingRegulationService.getScheduleRegulationsById(id);
        if(result instanceof MessageResponse){
            return new ResponseEntity<>(result, ((MessageResponse) result).getStatus());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/api/admin/schedule-keeping-regulation")
    public ResponseEntity<Object> addScheduleKeepingRegulation(@RequestBody ScheduleKeepingRegulationsRequest request){
        MessageResponse messageResponse = scheduleKeepingRegulationService.addScheduleRegulations(request);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }

    @PutMapping(value = "/api/admin/schedule-keeping-regulation")
    public ResponseEntity<Object> updateScheduleKeepingRegulation(@RequestBody ScheduleKeepingRegulationsRequest request){
        MessageResponse messageResponse = scheduleKeepingRegulationService.updateScheduleRegulations(request);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }

    @DeleteMapping(value = "/api/admin/schedule-keeping-regulation/id={id}")
    public ResponseEntity<Object> deleteScheduleKeepingRegulation(@PathVariable("id") Long id){
        MessageResponse messageResponse = scheduleKeepingRegulationService.deleteScheduleRegulations(id);
        return new ResponseEntity<>(messageResponse, messageResponse.getStatus());
    }
}
