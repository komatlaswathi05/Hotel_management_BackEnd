package com.HotelManagement.BillingService.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bills")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long billId;

    @Column(nullable = false, unique = true)
    private long bookingId;

    @Column(nullable = false)
    @Positive(message = "Amount must be positive")
    private double amount;

    @Column(nullable = false)
    @NotBlank(message = "Payment status is required")
    private String paymentStatus; // e.g., "Paid", "Pending", "Failed"
}
