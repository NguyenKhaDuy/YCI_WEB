package org.example.yci_web.Service;

import org.example.yci_web.Model.Request.RentalRegulationsRequest;
import org.example.yci_web.Model.Request.ScheduleKeepingRegulationsRequest;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Model.Response.MessageResponse;

public interface ScheduleKeepingRegulationService {
    DataResponse getScheduleRegulations();
    Object getScheduleRegulationsById(Long idScheduleRegulations);
    MessageResponse addScheduleRegulations(ScheduleKeepingRegulationsRequest scheduleKeepingRegulationsRequest);
    MessageResponse deleteScheduleRegulations(Long idScheduleRegulations);
    MessageResponse updateScheduleRegulations(ScheduleKeepingRegulationsRequest scheduleKeepingRegulationsRequest);
}
