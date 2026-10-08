package com.HotelManagement.RoomService.Feign;

import com.HotelManagement.RoomService.Entity.BookingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "BOOKING-CLIENT", url = "http://localhost:8082")
public interface BookingClient {

    @GetMapping("/api/booking/room/{roomNumber}")
    List<BookingEntity> getBookingsByRoomNumber(@PathVariable("roomNumber") Long roomNumber);
}
