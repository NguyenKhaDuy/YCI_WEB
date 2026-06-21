package org.example.yci_web.Controller;

import org.example.yci_web.Entity.BookingEntity;
import org.example.yci_web.Entity.PaymentBookingEntity;
import org.example.yci_web.Model.Response.DataResponse;
import org.example.yci_web.Repository.BookingRepository;
import org.example.yci_web.Repository.PaymentBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AnalyticsController {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    PaymentBookingRepository paymentBookingRepository;

    @GetMapping(value = "/api/admin/revenue/statistics")
    public ResponseEntity<Object> revenueStatistics() {
        List<BookingEntity> bookings = bookingRepository.findAll();
        List<PaymentBookingEntity> payments = paymentBookingRepository.findAll();
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Double> revenueByDay = new LinkedHashMap<>();
        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        Map<String, Double> revenueByYear = new LinkedHashMap<>();

        double totalBookingAmount = bookings.stream()
                .mapToDouble(booking -> booking.getTotalAmount() == null ? 0 : booking.getTotalAmount())
                .sum();
        double totalPaid = 0;
        double totalRemaining = 0;

        for (PaymentBookingEntity payment : payments) {
            double paid = payment.getPrice() == null ? 0 : payment.getPrice();
            totalPaid += paid;
            totalRemaining += payment.getRemainAmount() == null ? 0 : payment.getRemainAmount();
            LocalDate paidDate = payment.getPaidAt() == null ? LocalDate.now() : payment.getPaidAt().toLocalDate();
            YearMonth paidMonth = YearMonth.from(paidDate);
            String year = String.valueOf(paidDate.getYear());

            revenueByDay.merge(paidDate.toString(), paid, Double::sum);
            revenueByMonth.merge(paidMonth.toString(), paid, Double::sum);
            revenueByYear.merge(year, paid, Double::sum);
        }

        data.put("bookingCount", bookings.size());
        data.put("paymentCount", payments.size());
        data.put("totalBookingAmount", totalBookingAmount);
        data.put("totalPaid", totalPaid);
        data.put("totalRemaining", totalRemaining);
        data.put("revenueByDay", revenueByDay);
        data.put("revenueByMonth", revenueByMonth);
        data.put("revenueByYear", revenueByYear);

        DataResponse<Map<String, Object>> response = new DataResponse<>();
        response.setData(data);
        response.setMessage("Thành công");
        response.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
