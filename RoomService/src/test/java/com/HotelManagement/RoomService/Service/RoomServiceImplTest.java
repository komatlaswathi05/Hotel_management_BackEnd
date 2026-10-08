package com.HotelManagement.RoomService.Service;

import com.HotelManagement.RoomService.Entity.BookingEntity;
import com.HotelManagement.RoomService.Entity.RoomEntity;
import com.HotelManagement.RoomService.Exception.RoomNotFoundException;
import com.HotelManagement.RoomService.Feign.BookingClient;
import com.HotelManagement.RoomService.Repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceImplTest {

    @InjectMocks
    private RoomServiceImpl roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingClient bookingClient;

    private RoomEntity room;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        room = new RoomEntity();
        room.setRoomId(1L);
        room.setRoomType("Deluxe");
        room.setPrice(2000.0);
        room.setAvailability(true);
    }

    @Test
    void testInsertRoom_Success() {
        when(roomRepository.save(room)).thenReturn(room);

        RoomEntity saved = roomService.insertRoom(room);

        assertNotNull(saved);
        assertEquals("Deluxe", saved.getRoomType());
        verify(roomRepository).save(room);
    }

    @Test
    void testUpdateRoom_Success() {
        RoomEntity updated = new RoomEntity();
        updated.setRoomType("Suite");
        updated.setPrice(3000.0);
        updated.setAvailability(false);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(RoomEntity.class))).thenReturn(updated);

        roomService.updateRoom(1L, updated);

        assertEquals("Suite", room.getRoomType());
        assertEquals(3000.0, room.getPrice());
        assertFalse(room.isAvailability());
        verify(roomRepository).save(room);
    }

    @Test
    void testUpdateRoom_NotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(1L, room));
    }

    @Test
    void testDeleteRoom_Success() {
        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);

        roomService.deleteRoom(1L);

        verify(roomRepository).deleteById(1L);
    }

    @Test
    void testDeleteRoom_NotFound() {
        when(roomRepository.existsById(1L)).thenReturn(false);

        assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(1L));
    }

    @Test
    void testIsRoomAvailable_True() {
        BookingEntity booking1 = new BookingEntity();
        booking1.setCheckInDate(LocalDate.of(2025, 9, 1));
        booking1.setCheckOutDate(LocalDate.of(2025, 9, 5));
        booking1.setStatus("Cancelled"); // Not conflicting

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingClient.getBookingsByRoomId(1L)).thenReturn(List.of(booking1));

        boolean available = roomService.isRoomAvailable(1L, LocalDate.of(2025, 9, 12), LocalDate.of(2025, 9, 12));

        assertTrue(available);
    }

    @Test
    void testIsRoomAvailable_False_DueToOverlap() {
        BookingEntity booking1 = new BookingEntity();
        booking1.setCheckInDate(LocalDate.of(2025, 9, 10));
        booking1.setCheckOutDate(LocalDate.of(2025, 9, 15));
        booking1.setStatus("Confirmed");

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingClient.getBookingsByRoomId(1L)).thenReturn(List.of(booking1));

        boolean available = roomService.isRoomAvailable(1L, LocalDate.of(2025, 9, 12), LocalDate.of(2025, 9, 14));

        assertFalse(available);
    }

    @Test
    void testIsRoomAvailable_RoomNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () ->
                roomService.isRoomAvailable(1L, LocalDate.of(2025, 9, 10), LocalDate.of(2025, 9, 12)));
    }
}
