package com.HotelManagement.BookingService.Service;

import com.HotelManagement.BookingService.Entity.BookingEntity;

import java.util.List;

public interface BookingService {

    BookingEntity insertBooking(BookingEntity bookingEntity);

    List<BookingEntity> getAllBookings();

    void updateBooking(long bookingId,BookingEntity newBookingEntity);

    void deleteBooking(Long bookingId);

    void cancelBooking(Long bookingId);

    List<BookingEntity> getBookingHistoryByCustomerId(Long customerId);
}
