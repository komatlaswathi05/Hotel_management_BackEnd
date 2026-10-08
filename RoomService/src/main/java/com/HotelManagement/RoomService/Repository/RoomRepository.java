package com.HotelManagement.RoomService.Repository;

import com.HotelManagement.RoomService.Entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    List<RoomEntity> findByPrice(Double price);



}
