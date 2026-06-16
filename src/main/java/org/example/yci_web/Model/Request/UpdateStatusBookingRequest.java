package org.example.yci_web.Model.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusBookingRequest {
    private Long bookingId;
    private Long statusId;
}
