package com.HotelManagement.BookingService.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CUSTOMER-SERVICE", url = "http://localhost:8081")
public interface CustomerClient {
    @GetMapping("/api/customer/exists/{customerId}")
    CustomerDetails getCustomerById(@PathVariable("customerId") Long customerId);
}
