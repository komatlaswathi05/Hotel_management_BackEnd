package com.HotelManagement.RoomService.Entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingEntity {
    private Long bookingId;
    private Long customerId;
    private Long roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
}
