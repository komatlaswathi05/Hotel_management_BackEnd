package com.HotelManagement.BillingService.Service;

import com.HotelManagement.BillingService.DTO.BookingDTO;
import com.HotelManagement.BillingService.DTO.RoomDTO;
import com.HotelManagement.BillingService.Entity.BillingEntity;
import com.HotelManagement.BillingService.Exception.BillNotFoundException;
import com.HotelManagement.BillingService.Exception.BookingNotFoundException;
import com.HotelManagement.BillingService.Repository.BillingRepository;
import com.HotelManagement.BillingService.Feign.BookingClient;
import com.HotelManagement.BillingService.Feign.RoomClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceImplTest {

    @InjectMocks
    private BillingServiceImpl billingService;

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private RoomClient roomClient;

    @Mock
    private BookingClient bookingClient;

    private BillingEntity bill;

    private BookingDTO booking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        booking = new BookingDTO();
        booking.setBookingId(1L);
        booking.setRoomId(101L);
        booking.setCheckInDate(LocalDate.of(2025, 9, 10));
        booking.setCheckOutDate(LocalDate.of(2025, 9, 13));

        RoomDTO room = new RoomDTO();
        room.setRoomId(101L);
        room.setPrice(1000.0);

        when(roomClient.getRoomById(101L)).thenReturn(room);

        when(bookingClient.getBookingById(1L)).thenReturn(booking);

        bill = new BillingEntity();
        bill.setBillId(1L);
        bill.setBookingId(1L);
        bill.setAmount(3000.0);
        bill.setPaymentStatus("Pending");
    }


    @Test
    void testGenerateBillFromBooking_Success() {
        when(bookingClient.getBookingById(1L)).thenReturn(booking);
        when(billingRepository.save(any(BillingEntity.class))).thenReturn(bill);

        BillingEntity result = billingService.generateBillFromBooking(1L);

        assertNotNull(result);
        assertEquals(3000.0, result.getAmount());
        assertEquals("Pending", result.getPaymentStatus());
        verify(billingRepository).save(any(BillingEntity.class));
    }

    @Test
    void testGenerateBillFromBooking_BookingNotFound() {
        when(bookingClient.getBookingById(1L)).thenReturn(null);

        assertThrows(BookingNotFoundException.class, () -> billingService.generateBillFromBooking(1L));
    }

    @Test
    void testUpdateBill_Success() {
        BillingEntity updatedBill = new BillingEntity();
        updatedBill.setAmount(3500.0);
        updatedBill.setPaymentStatus("Paid");

        when(billingRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billingRepository.save(any(BillingEntity.class))).thenReturn(updatedBill);

        billingService.updateBill(1L,1L, updatedBill);

        assertEquals("Paid", bill.getPaymentStatus());
        assertEquals(3500.0, bill.getAmount());
        verify(billingRepository).save(bill);
    }

    @Test
    void testUpdateBill_NotFound() {
        when(billingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BillNotFoundException.class, () -> billingService.updateBill(1L, 1L,bill));
    }

    @Test
    void testDeleteBill_Success() {
        when(billingRepository.existsById(1L)).thenReturn(true);
        doNothing().when(billingRepository).deleteById(1L);

        billingService.deleteBill(1L);

        verify(billingRepository).deleteById(1L);
    }

    @Test
    void testDeleteBill_NotFound() {
        when(billingRepository.existsById(1L)).thenReturn(false);

        assertThrows(BillNotFoundException.class, () -> billingService.deleteBill(1L));
    }
}
