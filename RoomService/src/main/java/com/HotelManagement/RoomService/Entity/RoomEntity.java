package com.HotelManagement.RoomService.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roomNumber;

    @Column(nullable = false)
    @NotBlank(message = "Room type is required")
    private String roomType;

    @Column(nullable = false)
    @Positive(message = "Price must be positive")
    private Double price;

    @Column(nullable = false)
    private boolean availability;
}
