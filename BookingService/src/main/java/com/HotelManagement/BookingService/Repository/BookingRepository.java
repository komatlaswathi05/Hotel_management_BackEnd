package com.HotelManagement.BookingService.Repository;

import com.HotelManagement.BookingService.Entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByRoomNumber(Long roomNumber);

    List<BookingEntity> findByCustomerId(Long customerId);


}
