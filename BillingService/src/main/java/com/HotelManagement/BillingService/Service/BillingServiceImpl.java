package com.HotelManagement.BillingService.Service;

import com.HotelManagement.BillingService.DTO.BookingDTO;
import com.HotelManagement.BillingService.DTO.RoomDTO;
import com.HotelManagement.BillingService.Entity.BillingEntity;
import com.HotelManagement.BillingService.Exception.*;
import com.HotelManagement.BillingService.Feign.RoomClient;
import com.HotelManagement.BillingService.Repository.BillingRepository;
import com.HotelManagement.BillingService.Feign.BookingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImpl.class);

    @Autowired
    private BillingRepository billingRepository;

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private RoomClient roomClient;

    @Override
    public List<BillingEntity> getAllBills() {
        try {
            logger.info("Fetching all bills");
            return billingRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching bills", e);
            throw new RuntimeException("Unable to fetch bills at this time.");
        }
    }

    @Override
    public void updateBill(long billId, long bookingId, BillingEntity newBillingEntity) {
        try {
            logger.info("Updating bill with ID: {} and Booking ID: {}", billId, bookingId);

            BillingEntity existingBill = billingRepository.findByBillIdAndBookingId(billId, bookingId)
                    .orElseThrow(() -> new BillNotFoundException("Bill not found with Bill ID: " + billId + " and Booking ID: " + bookingId));

            double newAmount = newBillingEntity.getAmount();
            double originalAmount = existingBill.getAmount();

            if (newAmount < originalAmount) {
                throw new NoPartialAmountException("Your bill is " + originalAmount + ". Partial payment of " + newAmount + " is not allowed.");
            }
            if (newAmount > originalAmount) {
                throw new NoPartialAmountException("Your bill is " + originalAmount + ". payment of " + newAmount + " is not allowed.");
            }

            if ("Paid".equals(existingBill.getPaymentStatus())||"paid".equals(existingBill.getPaymentStatus())) {
                throw new PaymentAlreadyDoneException("Payment Already done");
            }

            existingBill.setAmount(newAmount);
            existingBill.setPaymentStatus(newBillingEntity.getPaymentStatus());

            billingRepository.save(existingBill);
            logger.debug("Bill updated: {}", existingBill);
        } catch (BillNotFoundException e) {
            logger.error("Bill not found", e);
            throw e;
        }catch (PaymentAlreadyDoneException e){
            logger.error("Payment already done", e);
            throw e;
        }
        catch (NoPartialAmountException e) {
            logger.error("Partial payment not allowed", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error updating bill", e);
            throw new RuntimeException("Unable to update bill.");
        }
    }


    @Override
    public void deleteBill(Long billId) {
        try {
            logger.info("Deleting bill with ID: {}", billId);
            if (billingRepository.existsById(billId)) {
                billingRepository.deleteById(billId);
                logger.debug("Bill deleted successfully");
            } else {
                throw new BillNotFoundException("Bill not found for deletion with ID: " + billId);
            }
        } catch (BillNotFoundException e) {
            logger.error("Bill not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting bill", e);
            throw new RuntimeException("Unable to delete bill.");
        }
    }

    @Override
    public BillingEntity generateBillFromBooking(Long bookingId) {
        try {
            logger.info("Generating bill from booking ID: {}", bookingId);
            BookingDTO booking = bookingClient.getBookingById(bookingId);
            if (booking == null) {
                throw new BookingNotFoundException("Booking not found with ID: " + bookingId);
            }

            List<BillingEntity> existingBills = billingRepository.findByBookingId(bookingId);
            if (!existingBills.isEmpty()) {
                throw new BillAlreadyGeneratedException("Bill already generated for booking ID: " + bookingId);
            }

            RoomDTO room = roomClient.getRoomNumber(booking.getRoomNumber());
            double ratePerDay = room.getPrice();

            long days = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            double amount = days * ratePerDay;

            BillingEntity bill = new BillingEntity();
            bill.setBookingId(bookingId);
            bill.setAmount(amount);
            bill.setPaymentStatus("Pending");

            BillingEntity savedBill = billingRepository.save(bill);
            logger.debug("Bill generated: {}", savedBill);
            return savedBill;

        } catch (BookingNotFoundException | BillAlreadyGeneratedException | PaymentAlreadyDoneException e) {
            logger.error("Error: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error generating bill", e);
            throw new RuntimeException("Unable to generate bill.");
        }
    }
}
