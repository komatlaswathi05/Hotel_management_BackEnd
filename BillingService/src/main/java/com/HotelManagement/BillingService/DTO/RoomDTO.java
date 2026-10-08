package com.HotelManagement.BillingService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    @Setter
    private long roomNumber;

    @NotBlank(message = "Room type is required")
    private String roomType;

    @Positive(message = "Price must be positive")
    private Double price;

    @Setter
    private boolean availability;

    public void setRoomType(@NotBlank(message = "Room type is required") String roomType) {
        this.roomType = roomType;
    }

    public void setPrice(@Positive(message = "Price must be positive") Double price) {
        this.price = price;
    }

}
