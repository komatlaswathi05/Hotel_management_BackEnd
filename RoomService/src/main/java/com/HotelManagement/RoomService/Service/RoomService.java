package com.HotelManagement.RoomService.Service;


import com.HotelManagement.RoomService.DTO.RoomDTO;
import com.HotelManagement.RoomService.Entity.RoomEntity;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    RoomEntity insertRoom(RoomEntity roomEntity);

    List<RoomEntity> getAllRooms();

    void updateRoom(long customerId,RoomEntity newRoomEntity);

    void deleteRoom(Long roomId);

    List<RoomDTO> getAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);
}
