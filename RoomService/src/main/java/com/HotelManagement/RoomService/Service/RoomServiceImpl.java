package com.HotelManagement.RoomService.Service;

import com.HotelManagement.RoomService.DTO.RoomDTO;
import com.HotelManagement.RoomService.Entity.BookingEntity;
import com.HotelManagement.RoomService.Entity.RoomEntity;
import com.HotelManagement.RoomService.Exception.InvalidDatesException;
import com.HotelManagement.RoomService.Exception.RoomNotFoundException;
import com.HotelManagement.RoomService.Feign.BookingClient;
import com.HotelManagement.RoomService.Repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomServiceImpl.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingClient bookingClient;

    @Override
    public RoomEntity insertRoom(RoomEntity roomEntity) {
        try {
            logger.info("Saving new room: {}", roomEntity);
            return roomRepository.save(roomEntity);
        } catch (Exception e) {
            logger.error("Error saving room", e);
            throw new RuntimeException("Unable to save room.");
        }
    }

    @Override
    public List<RoomEntity> getAllRooms() {
        try {
            logger.info("Fetching all rooms");
            return roomRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching rooms", e);
            throw new RuntimeException("Unable to fetch rooms.");
        }
    }

    @Override
    public void updateRoom(long roomId, RoomEntity newRoomEntity) {
        try {
            logger.info("Updating room with ID: {}", roomId);

            RoomEntity existingRoom = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RoomNotFoundException("Room not found with ID: " + roomId));

            existingRoom.setRoomNumber(roomId);
            if (newRoomEntity.getRoomType() != null) {
                existingRoom.setRoomType(newRoomEntity.getRoomType());
            }

            if (newRoomEntity.getPrice() != null) {
                existingRoom.setPrice(newRoomEntity.getPrice());
            }

            existingRoom.setAvailability(newRoomEntity.isAvailability());

            RoomEntity updatedRoom = roomRepository.save(existingRoom);
            logger.debug("Room updated: {}", updatedRoom);
        } catch (RoomNotFoundException e) {
            logger.error("Room not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error updating room", e);
            throw new RuntimeException("Unable to update room.");
        }
    }

    @Override
    public void deleteRoom(Long roomId) {
        try {
            logger.info("Deleting room with ID: {}", roomId);
            if (roomRepository.existsById(roomId)) {
                roomRepository.deleteById(roomId);
                logger.debug("Room deleted successfully");
            } else {
                throw new RoomNotFoundException("Room not found for deletion with ID: " + roomId);
            }
        } catch (RoomNotFoundException e) {
            logger.error("Room not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting room", e);
            throw new RuntimeException("Unable to delete room.");
        }
    }

    @Override
    public List<RoomDTO> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<RoomDTO> availableRooms = new ArrayList<>();
        try {
            logger.info("Checking availability of rooms between: {} and {}", checkIn, checkOut);

            if (checkIn.isBefore(LocalDate.now())) {
                throw new InvalidDatesException("Date should be today's date or advance");
            }
            if (checkOut.isBefore(checkIn)) {
                throw new InvalidDatesException("Check-out date should be greater than check-in date");
            }

            List<RoomEntity> allRooms = roomRepository.findAll();

            for (RoomEntity room : allRooms) {
                if (room.isAvailability()) {
                    List<BookingEntity> bookings = bookingClient.getBookingsByRoomNumber(room.getRoomNumber());

                    boolean isBooked = bookings.stream().anyMatch(booking ->
                            booking.getStatus().equalsIgnoreCase("Confirmed") &&
                                    !(checkOut.isBefore(booking.getCheckInDate()) || checkIn.isAfter(booking.getCheckOutDate()))
                    );

                    if (!isBooked) {
                        RoomDTO dto = new RoomDTO();
                        dto.setRoomNumber(room.getRoomNumber());
                        dto.setRoomType(room.getRoomType());
                        dto.setPrice(room.getPrice());
                        dto.setAvailability(room.isAvailability());
                        availableRooms.add(dto);
                    }
                }
            }

        } catch (InvalidDatesException e) {
            logger.error("Invalid date range provided: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error while checking available rooms", e);
            throw new RuntimeException("Unable to fetch available rooms at the moment.");
        }

        return availableRooms;
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {

        try {
            logger.info("Checking availability for room ID: {} between {} and {}", roomId, checkIn, checkOut);

            RoomEntity room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RoomNotFoundException("Room not found with ID: " + roomId));
            if(checkIn.isBefore(LocalDate.now())){
                throw new InvalidDatesException("Date should be today's date or advance");
            }
            if(checkOut.isBefore(checkIn)){
                throw new InvalidDatesException("check out date should be greater than checkin date");
            }
            List<BookingEntity> bookings = bookingClient.getBookingsByRoomNumber(roomId);

            for (BookingEntity booking : bookings) {
                boolean alreadyBooked = checkIn.isBefore(booking.getCheckOutDate()) && checkOut.isAfter(booking.getCheckInDate());
                if (alreadyBooked && booking.getStatus().equalsIgnoreCase("Confirmed")) {
                    logger.warn("Room {} is already booked during the requested period", roomId);
                    return false;
                }
            }
            return true;
        } catch (RoomNotFoundException e) {
            logger.error("Room not found", e);
            throw e;
        } catch (InvalidDatesException e){
            logger.error("Date must be greater than todays date");
            throw e;
        }catch (Exception e) {
            logger.error("Error checking room availability", e);
            throw new RuntimeException("Unable to check room availability.");
        }
    }

}
