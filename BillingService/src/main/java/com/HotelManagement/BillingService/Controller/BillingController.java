package com.HotelManagement.BillingService.Controller;

import com.HotelManagement.BillingService.Entity.BillingEntity;
import com.HotelManagement.BillingService.Service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@Tag(name = "Billing Controller", description = "Operations related to billing")
public class BillingController {

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    @Autowired
    private BillingService billingService;


    @PostMapping("/generate")
    @Operation(summary = "Generate bill from booking", description = "Generates a bill based on booking ID")
    public ResponseEntity<BillingEntity> generateBill(@RequestParam Long bookingId) {
        logger.info("Generating bill for booking ID: {}", bookingId);
        BillingEntity bill = billingService.generateBillFromBooking(bookingId);
        logger.debug("Generated bill: {}", bill);
        return new ResponseEntity<>(bill, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all bills", description = "Retrieves all bill records")
    public ResponseEntity<List<BillingEntity>> getAllBills(){
        logger.info("Fetching all bills");
        List<BillingEntity> getAllBills = billingService.getAllBills();
        logger.debug("Total Bills fetched: {}", getAllBills.size());
        return new ResponseEntity<>(getAllBills, HttpStatus.OK);
    }

    @PutMapping("/update")
    @Operation(summary = "Update bill", description = "Updates bill details by ID and Booking ID")
    public ResponseEntity<String> updateBill(@RequestParam long billId,
                                             @RequestParam long bookingId,
                                             @Valid @RequestBody BillingEntity newBillingEntity) {
        logger.info("Updating bill with ID: {}", billId);
        billingService.updateBill(billId, bookingId, newBillingEntity);
        logger.debug("Bill updated successfully");
        return new ResponseEntity<>("Bill Successfully paid", HttpStatus.ACCEPTED);
    }


    @DeleteMapping("delete")
    @Operation(summary = "Delete Bill", description = "Deletes bill by ID")
    public ResponseEntity<String> deleteBill(@RequestParam Long billId){
        logger.info("Deleting bill with ID: {}", billId);
        billingService.deleteBill(billId);
        logger.debug("Bill deleted successfully");
        return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
    }
}
