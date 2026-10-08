package com.HotelManagement.BookingService.Service;

import com.HotelManagement.BookingService.Entity.BookingEntity;
import com.HotelManagement.BookingService.Exception.*;
import com.HotelManagement.BookingService.Feign.CustomerClient;
import com.HotelManagement.BookingService.Feign.CustomerDetails;
import com.HotelManagement.BookingService.Feign.RoomClient;
import com.HotelManagement.BookingService.Repository.BookingRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private RoomClient roomClient;

    private BookingEntity booking;

    private static FeignException.NotFound customerNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/customer/exists/101",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.NotFound("Not Found", request, null, null);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        booking = new BookingEntity();
        booking.setBookingId(1L);
        booking.setCustomerId(101L);
        booking.setRoomNumber(202L);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(2));
        booking.setStatus("Pending");
    }

    @Test
    void testInsertBooking_Success() {
        when(customerClient.getCustomerById(booking.getCustomerId())).thenReturn(new CustomerDetails());
        when(roomClient.isRoomAvailable(booking.getRoomNumber(), booking.getCheckInDate(), booking.getCheckOutDate())).thenReturn(true);
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(booking);

        BookingEntity result = bookingService.insertBooking(booking);

        assertNotNull(result);
        assertEquals("Confirmed", result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void testInsertBooking_CustomerNotFound() {
        when(customerClient.getCustomerById(booking.getCustomerId())).thenThrow(customerNotFoundException());

        assertThrows(CustomerNotFoundException.class, () -> bookingService.insertBooking(booking));
    }

    @Test
    void testInsertBooking_RoomUnavailable() {
        when(customerClient.getCustomerById(booking.getCustomerId())).thenReturn(new CustomerDetails());
        when(roomClient.isRoomAvailable(booking.getRoomNumber(), booking.getCheckInDate(), booking.getCheckOutDate())).thenReturn(false);

        assertThrows(RoomUnavailableException.class, () -> bookingService.insertBooking(booking));
    }

    @Test
    void testInsertBooking_InvalidDates() {
        booking.setCheckOutDate(LocalDate.of(2025, 9, 10)); // before check-in

        when(customerClient.getCustomerById(booking.getCustomerId())).thenReturn(new CustomerDetails());
        when(roomClient.isRoomAvailable(booking.getRoomNumber(), booking.getCheckInDate(), booking.getCheckOutDate())).thenReturn(true);

        assertThrows(InvalidBookingDatesException.class, () -> bookingService.insertBooking(booking));
    }

    @Test
    void testCancelBooking_Success() {
        booking.setStatus("Confirmed");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(booking);

        bookingService.cancelBooking(1L);

        assertEquals("Cancelled", booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        booking.setStatus("Cancelled");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(RuntimeException.class, () -> bookingService.cancelBooking(1L));
    }

    @Test
    void testCancelBooking_NotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.cancelBooking(1L));
    }
}
