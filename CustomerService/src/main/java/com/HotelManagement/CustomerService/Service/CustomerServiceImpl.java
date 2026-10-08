package com.HotelManagement.CustomerService.Service;

import com.HotelManagement.CustomerService.Entity.CustomerEntity;
import com.HotelManagement.CustomerService.Entity.LoginRequest;
import com.HotelManagement.CustomerService.Entity.LoginResponse;
import com.HotelManagement.CustomerService.Entity.MessageResponse;
import com.HotelManagement.CustomerService.Entity.ResetPasswordRequest;
import com.HotelManagement.CustomerService.Exception.CustomerAlreadyExistsException;
import com.HotelManagement.CustomerService.Exception.CustomerNotFoundException;
import com.HotelManagement.CustomerService.Exception.InvalidCredentialsException;
import com.HotelManagement.CustomerService.Repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public CustomerEntity registerCustomer(CustomerEntity customerEntity) {
        try {
            logger.info("Registering Customer: {}", customerEntity.getUsername());

            boolean emailExists = customerRepository.existsByEmail(customerEntity.getEmail());
            boolean phoneExists = customerRepository.existsByPhoneNumber(customerEntity.getPhoneNumber());
            boolean usernameExists = customerRepository.existsByUsername(customerEntity.getUsername());

            if (emailExists || phoneExists || usernameExists) {
                throw new CustomerAlreadyExistsException("Customer with username, email or phone number already exists.");
            }

            customerEntity.setPassword(passwordEncoder.encode(customerEntity.getPassword()));

            return customerRepository.save(customerEntity);
        } catch (CustomerAlreadyExistsException e) {
            logger.warn("Customer already exists: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error registering customer", e);
            throw new RuntimeException("Unable to register customer.");
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Login attempt for: {}", loginRequest.getUsernameOrEmail());

        Optional<CustomerEntity> customerOptional = customerRepository.findByUsername(loginRequest.getUsernameOrEmail());
        boolean loggedInWithUsername = customerOptional.isPresent();

        if (customerOptional.isEmpty()) {
            customerOptional = customerRepository.findByEmail(loginRequest.getUsernameOrEmail());
        }

        CustomerEntity customer = customerOptional
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username/email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        logger.debug("Login successful for customer ID: {}", customer.getCustomerId());

        String message = loggedInWithUsername
                ? "Customer login successfully with username and password"
                : "Customer login successfully with email and password";

        return new LoginResponse(message, customer);
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        CustomerEntity customer = customerRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new CustomerNotFoundException("No customer found with that email."));

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);

        return new MessageResponse("Password reset successfully. You can now log in with your new password.");
    }

    @Override
    public List<CustomerEntity> getAllCustomers() {
        try {
            logger.info("Fetching all customers");
            return customerRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching customers", e);
            throw new RuntimeException("Unable to fetch customers.");
        }
    }

    @Override
    public CustomerEntity getCustomerById(long customerId) {
        logger.info("Fetching customer with ID: {}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }

    @Override
    public void updateCustomer(long customerId, CustomerEntity newCustomerEntity) {
        try {
            logger.info("Updating customer with ID: {}", customerId);
            CustomerEntity existingCustomer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

            existingCustomer.setCustomerId(customerId);
            if (newCustomerEntity.getUsername() != null) {
                existingCustomer.setUsername(newCustomerEntity.getUsername());
            }
            if (newCustomerEntity.getEmail() != null) {
                existingCustomer.setEmail(newCustomerEntity.getEmail());
            }
            if (newCustomerEntity.getPhoneNumber() != null) {
                existingCustomer.setPhoneNumber(newCustomerEntity.getPhoneNumber());
            }
            if (newCustomerEntity.getPassword() != null && !newCustomerEntity.getPassword().isBlank()) {
                existingCustomer.setPassword(passwordEncoder.encode(newCustomerEntity.getPassword()));
            }

            customerRepository.save(existingCustomer);
            logger.debug("Customer updated: {}", existingCustomer.getCustomerId());
        } catch (CustomerNotFoundException e) {
            logger.error("Customer not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error updating customer", e);
            throw new RuntimeException("Unable to update customer.");
        }
    }

    @Override
    public void deleteCustomer(Long customerId) {
        try {
            logger.info("Deleting customer with ID: {}", customerId);
            if (customerRepository.existsById(customerId)) {
                customerRepository.deleteById(customerId);
                logger.debug("Customer deleted successfully");
            } else {
                throw new CustomerNotFoundException("Customer not found for deletion with ID: " + customerId);
            }
        } catch (CustomerNotFoundException e) {
            logger.error("Customer not found", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting customer", e);
            throw new RuntimeException("Unable to delete customer.");
        }
    }
}
