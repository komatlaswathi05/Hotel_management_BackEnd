package com.HotelManagement.BillingService.Service;

import com.HotelManagement.BillingService.Entity.BillingEntity;

import java.util.List;

public interface BillingService {

    List<BillingEntity> getAllBills();

    void updateBill(long billId,long bookingId, BillingEntity newBillingEntity);

    void deleteBill(Long billId);

    BillingEntity generateBillFromBooking(Long bookingId);

}
