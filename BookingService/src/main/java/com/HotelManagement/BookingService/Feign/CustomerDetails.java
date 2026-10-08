package com.HotelManagement.BookingService.Feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight representation of customer details returned by CustomerService,
 * used by BookingService to validate a customer before creating a booking.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetails {
    private long customerId;
    private String username;
    private String email;
    private Long phoneNumber;
}
