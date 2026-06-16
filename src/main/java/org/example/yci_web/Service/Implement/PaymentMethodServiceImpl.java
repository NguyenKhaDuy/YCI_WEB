package org.example.yci_web.Service.Implement;

import org.example.yci_web.Entity.PaymentsMethodEntity;
import org.example.yci_web.Model.DTO.PaymentMethodDTO;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Repository.PaymentsMethodRepository;
import org.example.yci_web.Service.PaymentMethodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {
    @Autowired
    PaymentsMethodRepository paymentsMethodRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public DataResponse getPaymentMethods() {
        DataResponse dataResponse = new DataResponse();
        List<PaymentsMethodEntity> paymentsMethodEntities = paymentsMethodRepository.findAll();
        List<PaymentMethodDTO> paymentMethodDTOS = new ArrayList<>();
        for (PaymentsMethodEntity paymentsMethodEntity : paymentsMethodEntities) {
            PaymentMethodDTO paymentMethodDTO = new PaymentMethodDTO();
            modelMapper.map(paymentsMethodEntity, paymentMethodDTO);
            paymentMethodDTOS.add(paymentMethodDTO);
        }
        dataResponse.setData(paymentMethodDTOS);
        dataResponse.setMessage("Success");
        dataResponse.setStatus(HttpStatus.OK);
        return dataResponse;
    }
}
