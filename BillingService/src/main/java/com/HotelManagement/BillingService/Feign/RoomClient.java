package com.HotelManagement.BillingService.Feign;

import com.HotelManagement.BillingService.DTO.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "room-service", url = "http://localhost:8083")
public interface RoomClient {

    @GetMapping("/api/room/{roomNumber}")
    RoomDTO getRoomNumber(@PathVariable("roomNumber") Long roomNumber);
}

