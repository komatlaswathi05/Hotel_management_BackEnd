package com.HotelManagement.CustomerService.Repository;

import com.HotelManagement.CustomerService.Entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(Long phoneNumber);
    boolean existsByUsername(String username);

    Optional<CustomerEntity> findByUsername(String username);
    Optional<CustomerEntity> findByEmail(String email);
    Optional<CustomerEntity> findByEmailIgnoreCase(String email);
}
