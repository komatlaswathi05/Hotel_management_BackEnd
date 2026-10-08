package com.HotelManagement.BookingService.Controller;

import com.HotelManagement.BookingService.Entity.BookingEntity;
import com.HotelManagement.BookingService.Repository.BookingRepository;
import com.HotelManagement.BookingService.Service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
@Tag(name = "Booking Controller", description = "Operations related to booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping
    @Operation(summary = "Insert a new booking", description = "Creates a new booking record")
    public ResponseEntity<BookingEntity> insertBooking(@Valid @RequestBody BookingEntity bookingEntity){
        logger.info("Inserting new booking {}", bookingEntity);
        BookingEntity insertBooking = bookingService.insertBooking(bookingEntity);
        logger.debug("Inserted booking with ID: {}", insertBooking.getBookingId());
        return new ResponseEntity<>(insertBooking, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Retrieves all booking records")
    public ResponseEntity<List<BookingEntity>> getAllBookings(){
        logger.info("Fetching all bookings");
        List<BookingEntity> getAllBookings = bookingService.getAllBookings();
        logger.debug("Total bookings fetched: {}", getAllBookings.size());
        return new ResponseEntity<>(getAllBookings, HttpStatus.OK);
    }

    @PutMapping("/update/{bookingId}")
    @Operation(summary = "Update booking", description = "Updates booking details by ID")
    public ResponseEntity<String> updateBooking(@PathVariable long bookingId, @RequestBody BookingEntity newBookingEntity) {
        logger.info("Updating booking with ID: {}", bookingId);
        bookingService.updateBooking(bookingId, newBookingEntity);
        logger.debug("Booking updated successfully");
        return new ResponseEntity<>("Updated Successfully", HttpStatus.ACCEPTED);
    }

    @PutMapping("/cancel/{bookingId}")
    @Operation(summary = "Cancel booking", description = "Cancels a booking by ID")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return new ResponseEntity<>("Booking cancelled successfully", HttpStatus.OK);
    }

    @GetMapping("/room/{roomNumber}")
    public List<BookingEntity> getBookingsByRoomNumber(@PathVariable Long roomNumber) {
        return bookingRepository.findByRoomNumber(roomNumber);
    }

    @GetMapping("/history")
    @Operation(summary = "View booking history", description = "Retrieves all bookings made by a specific customer")
    public ResponseEntity<List<BookingEntity>> getBookingHistory(@RequestParam Long customerId) {
        logger.info("Fetching booking history for customer ID: {}", customerId);
        List<BookingEntity> history = bookingService.getBookingHistoryByCustomerId(customerId);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{bookingId}")
    @Operation(summary = "Delete booking", description = "Deletes booking by ID")
    public ResponseEntity<String> deleteBooking(@PathVariable Long bookingId){
        logger.info("Deleting booking with ID: {}", bookingId);
        bookingService.deleteBooking(bookingId);
        logger.debug("Booking deleted successfully");
        return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public BookingEntity getBookingById(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }



}
