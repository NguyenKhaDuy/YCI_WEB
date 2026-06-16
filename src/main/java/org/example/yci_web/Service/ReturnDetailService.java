package org.example.yci_web.Service;

import org.example.yci_web.Model.Request.ReturnDetailRequest;
import org.example.yci_web.Model.Response.MessageResponse;

public interface ReturnDetailService {
    MessageResponse addReturnDetail(ReturnDetailRequest returnDetailRequest);

}
