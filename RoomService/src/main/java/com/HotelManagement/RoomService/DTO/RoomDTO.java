package com.HotelManagement.RoomService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private long roomNumber;

    @NotBlank(message = "Room type is required")
    private String roomType;

    @Positive(message = "Price must be positive")
    private Double price;

    private boolean availability;


}
