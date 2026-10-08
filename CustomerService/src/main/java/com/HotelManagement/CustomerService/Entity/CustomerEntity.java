package com.HotelManagement.CustomerService.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long customerId;

    @Column(nullable = false, unique = true)
    @Size(min = 3, message = "Username must be at least 3 characters")
    @Size(max = 20, message = "Username should be less than 20 characters")
    @NotBlank(message = "Username is required")
    private String username;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(nullable = false, unique = true)
    @Min(value = 1000000000L, message = "Phone number must be exactly 10 digits")
    @Max(value = 9999999999L, message = "Phone number must be exactly 10 digits")
    private Long phoneNumber;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
