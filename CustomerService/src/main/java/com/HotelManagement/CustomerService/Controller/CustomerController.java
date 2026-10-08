package com.HotelManagement.CustomerService.Controller;

import com.HotelManagement.CustomerService.Entity.CustomerEntity;
import com.HotelManagement.CustomerService.Entity.LoginRequest;
import com.HotelManagement.CustomerService.Entity.LoginResponse;
import com.HotelManagement.CustomerService.Entity.MessageResponse;
import com.HotelManagement.CustomerService.Entity.ResetPasswordRequest;
import com.HotelManagement.CustomerService.Service.CustomerService;
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
@RequestMapping("/api/customer")
@Tag(name = "Customer Controller", description = "Operations related to customer registration and login")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @PostMapping("/signup")
    @Operation(summary = "Customer Signup", description = "Customer registers using username, email, phone number and password")
    public ResponseEntity<CustomerEntity> registerCustomer(@Valid @RequestBody CustomerEntity customerEntity){
        logger.info("Customer Signup: {}", customerEntity.getUsername());
        CustomerEntity insertCustomer = customerService.registerCustomer(customerEntity);
        logger.debug("Customer Registered with Id: {}", insertCustomer.getCustomerId());
        return new ResponseEntity<>(insertCustomer, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Customer Login", description = "Customer logs in using username/email and password")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        logger.info("Login attempt: {}", loginRequest.getUsernameOrEmail());
        LoginResponse loginResponse = customerService.login(loginRequest);
        logger.debug("Login successful for customer ID: {}", loginResponse.getCustomer().getCustomerId());
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Sets a new password for the customer identified by email")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(customerService.resetPassword(request));
    }

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieves all customer records")
    public ResponseEntity<List<CustomerEntity>> getAllCustomers(){
        logger.info("Fetching all customers");
        List<CustomerEntity> getAllCustomers = customerService.getAllCustomers();
        logger.debug("Total customers fetched: {}", getAllCustomers.size());
        return new ResponseEntity<>(getAllCustomers, HttpStatus.OK);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID", description = "Retrieves full customer details by ID")
    public ResponseEntity<CustomerEntity> getCustomerById(@PathVariable long customerId){
        logger.info("Fetching customer with ID: {}", customerId);
        CustomerEntity customer = customerService.getCustomerById(customerId);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PutMapping("/update/{customerId}")
    @Operation(summary = "Update Customer Records", description = "Updates customer details by ID")
    public ResponseEntity<String> updateCustomer(@PathVariable long customerId, @RequestBody CustomerEntity newCustomerEntity) {
        logger.info("Updating customer with ID: {}", customerId);
        customerService.updateCustomer(customerId, newCustomerEntity);
        logger.debug("Customer updated successfully");
        return new ResponseEntity<>("Successfully Updated", HttpStatus.ACCEPTED);
    }


    @DeleteMapping("delete/{customerId}")
    @Operation(summary = "Delete customer", description = "Deletes customer by ID")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long customerId){
        logger.info("Deleting customer with ID: {}", customerId);
        customerService.deleteCustomer(customerId);
        logger.debug("Customer deleted successfully");
        return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
    }

    @GetMapping("/exists/{customerId}")
    @Operation(summary = "Check customer existence", description = "Returns customer details if the customer exists; 404 if not found")
    public ResponseEntity<CustomerEntity> existsById(@PathVariable Long customerId) {
        logger.info("Checking existence for customer ID: {}", customerId);
        CustomerEntity customer = customerService.getCustomerById(customerId);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

}
