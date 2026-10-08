package com.HotelManagement.BookingService.Service;

import com.HotelManagement.BookingService.Entity.BookingEntity;
import com.HotelManagement.BookingService.Exception.*;
import com.HotelManagement.BookingService.Feign.CustomerClient;
import com.HotelManagement.BookingService.Feign.RoomClient;
import com.HotelManagement.BookingService.Repository.BookingRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomClient roomClient;

    @Autowired
    private CustomerClient customerClient;

    @Override
    public BookingEntity insertBooking(BookingEntity bookingEntity) {

        try {
            logger.info("Creating booking for customer ID: {}", bookingEntity.getCustomerId());

            try {
                customerClient.getCustomerById(bookingEntity.getCustomerId());
            } catch (FeignException.NotFound ex) {
                throw new CustomerNotFoundException("Customer does not exist.");
            }

            if (bookingEntity.getCheckInDate().isBefore(LocalDate.now())) {
                throw new InvalidBookingDatesException("Check-in date cannot be in the past.");
            }

            if (!bookingEntity.getCheckOutDate().isAfter(bookingEntity.getCheckInDate())) {
                throw new InvalidBookingDatesException("Check-out date must be after check-in date.");
            }

            boolean isAvailable = roomClient.isRoomAvailable(
                    bookingEntity.getRoomNumber(),
                    bookingEntity.getCheckInDate(),
                    bookingEntity.getCheckOutDate()
            );

            if (!isAvailable) {
                throw new RoomUnavailableException("Room is already booked.");
            }

            bookingEntity.setStatus("Confirmed");
            return bookingRepository.save(bookingEntity);

        } catch (CustomerNotFoundException | RoomUnavailableException | InvalidBookingDatesException e) {
            logger.error("Booking creation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during booking creation", e);
            throw new RuntimeException("Unable to create booking.");
        }
    }

    @Override
    public List<BookingEntity> getAllBookings() {
        try {
            logger.info("Fetching all bookings");
            return bookingRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching bookings", e);
            throw new RuntimeException("Unable to fetch bookings.");
        }
    }

    @Override
    public List<BookingEntity> getBookingHistoryByCustomerId(Long customerId) {
        try {
            logger.info("Fetching booking history for customer ID: {}", customerId);
            return bookingRepository.findByCustomerId(customerId);
        } catch (Exception e) {
            logger.error("Error fetching booking history", e);
            throw new RuntimeException("Unable to fetch booking history.");
        }
    }

    @Override
    public void updateBooking(long bookingId, BookingEntity newBookingEntity) {
        try {
            logger.info("Updating booking with ID: {}", bookingId);
            BookingEntity existingBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

            existingBooking.setBookingId(bookingId);
            if(newBookingEntity.getCheckInDate().isBefore(LocalDate.now())){
                throw new InvalidBookingDatesException("Date should be today's date or advance");
            }
            if(newBookingEntity.getCheckOutDate().isBefore(newBookingEntity.getCheckInDate())){
                throw new InvalidBookingDatesException("check out date should be greater than checkin date");
            }
            if(newBookingEntity.getCheckInDate()!=null){
            existingBooking.setCheckInDate(newBookingEntity.getCheckInDate());
            }
            if(newBookingEntity.getCheckOutDate()!=null) {
                existingBooking.setCheckOutDate(newBookingEntity.getCheckOutDate());
            }
            bookingRepository.save(existingBooking);
            logger.debug("Booking updated: {}", existingBooking);
        } catch (BookingNotFoundException e) {
            logger.error("Booking not found", e);
            throw e;
        }catch (InvalidBookingDatesException e){
            logger.error("Date should be provide properly", e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Error updating booking", e);
            throw new RuntimeException("Unable to update booking.");
        }
    }

    @Override
    public void deleteBooking(Long bookingId) {
        try {
            logger.info("Deleting booking with ID: {}", bookingId);
            if (bookingRepository.existsById(bookingId)) {
                bookingRepository.deleteById(bookingId);
                logger.debug("Booking deleted successfully");
            } else {
                throw new BookingNotFoundException("Booking not found for deletion with ID: " + bookingId);
            }
        } catch (BookingNotFoundException e) {
            logger.error("Booking not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting booking", e);
            throw new RuntimeException("Unable to delete booking.");
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        try {
            logger.info("Cancelling booking with ID: {}", bookingId);
            BookingEntity booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

            if ("Cancelled".equalsIgnoreCase(booking.getStatus())) {
                throw new RuntimeException("Booking is already cancelled");
            }

            booking.setStatus("Cancelled");
            bookingRepository.save(booking);
            logger.debug("Booking cancelled successfully: {}", bookingId);
        } catch (BookingNotFoundException e) {
            logger.error("Booking not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error cancelling booking", e);
            throw new RuntimeException("Unable to cancel booking.");
        }
    }
}
