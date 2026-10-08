package com.HotelManagement.BillingService.Repository;

import com.HotelManagement.BillingService.Entity.BillingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<BillingEntity, Long> {
    List<BillingEntity> findByBookingId(Long bookingId);
    Optional<BillingEntity> findByBillIdAndBookingId(Long billId, Long bookingId);
}

