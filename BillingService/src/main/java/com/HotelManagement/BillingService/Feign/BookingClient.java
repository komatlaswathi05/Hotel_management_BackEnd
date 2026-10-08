package com.HotelManagement.BillingService.Feign;

import com.HotelManagement.BillingService.DTO.BookingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOOKING-SERVICE", url = "http://localhost:8082")
public interface BookingClient {
    @GetMapping("/api/booking/{bookingId}")
    BookingDTO getBookingById(@PathVariable("bookingId") Long bookingId);
}


