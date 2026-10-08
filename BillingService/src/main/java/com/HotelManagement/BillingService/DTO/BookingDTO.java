package com.HotelManagement.BillingService.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingDTO {
    private long bookingId;
    private long customerId;
    private long roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    @NotBlank(message = "Booking status is required")
    private String status;
}
