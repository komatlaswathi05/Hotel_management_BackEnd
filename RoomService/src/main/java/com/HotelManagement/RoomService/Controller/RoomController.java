package com.HotelManagement.RoomService.Controller;

import com.HotelManagement.RoomService.DTO.RoomDTO;
import com.HotelManagement.RoomService.Entity.RoomEntity;
import com.HotelManagement.RoomService.Exception.InvalidDatesException;
import com.HotelManagement.RoomService.Exception.RoomNotFoundException;
import com.HotelManagement.RoomService.Repository.RoomRepository;
import com.HotelManagement.RoomService.Service.RoomService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room")
@Tag(name = "Room Controller", description = "Operations related to Room management")
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @PostMapping
    @Operation(summary = "Insert a new room", description = "Creates a new Room record")
    public ResponseEntity<RoomEntity> insertRoom(@Valid @RequestBody RoomEntity roomEntity){
        logger.info("Inserting new room: {}", roomEntity);
        RoomEntity insertRoom = roomService.insertRoom(roomEntity);
        logger.debug("Inserted room with ID: {}", insertRoom.getRoomNumber());
        return new ResponseEntity<>(insertRoom, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all rooms", description = "Retrieves all room records")
    public ResponseEntity<List<RoomEntity>> getAllRoom(){
        logger.info("Fetching all rooms");
        List<RoomEntity> getAllRooms = roomService.getAllRooms();
        logger.debug("Total Rooms fetched: {}", getAllRooms.size());
        return new ResponseEntity<>(getAllRooms, HttpStatus.OK);
    }

    @Operation(summary = "Retrieves specific room", description = "Returns room by room Number")
    @GetMapping("/{roomNumber}")
    public ResponseEntity<RoomEntity> getByRoomNumber(@PathVariable Long roomNumber) {
        RoomEntity getRoomByRoomNumber=roomRepository.findById(roomNumber).orElseThrow(()->new RoomNotFoundException("Room not found with room Number:"+roomNumber));
        return new ResponseEntity<>(getRoomByRoomNumber,HttpStatus.OK);
    }

    @Operation(summary = "Check availability of a specific room", description = "Returns true if the room is available, false otherwise")
    @GetMapping("/available/{roomNumber}")
    public ResponseEntity<Boolean> getAvailableRoom(
            @PathVariable Long roomNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        boolean isAvailable = roomService.isRoomAvailable(roomNumber, checkIn, checkOut);
        return ResponseEntity.ok(isAvailable);
    }




    @Operation(summary = "Get all available rooms", description = "Returns a list of rooms available between the given check-in and check-out dates")
    @GetMapping("/availablerooms")
    public ResponseEntity<List<RoomDTO>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        List<RoomDTO> availableRooms = roomService.getAvailableRooms(checkIn, checkOut);
        return new ResponseEntity<>(availableRooms, HttpStatus.OK);
    }


    @PutMapping("/update/{roomId}")
    @Operation(summary = "Update room", description = "Updates room details by ID")
    public ResponseEntity<String> updateRoom(@PathVariable long roomId, @Valid @RequestBody RoomEntity newRoomEntity) {
        logger.info("Updating room with ID: {}", roomId);
        roomService.updateRoom(roomId, newRoomEntity);
        logger.debug("Room updated successfully");
        return new ResponseEntity<>("Fields Updated Successfully", HttpStatus.ACCEPTED);
    }

    @DeleteMapping("delete/{roomId}")
    @Operation(summary = "Delete room", description = "Deletes room by ID")
    public ResponseEntity<String> deleteRoom(@PathVariable Long roomId){
        logger.info("Deleting room with ID: {}", roomId);
        roomService.deleteRoom(roomId);
        logger.debug("Room deleted successfully");
        return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
    }
}
