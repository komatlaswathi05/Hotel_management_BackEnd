package com.HotelManagement.CustomerService.Service;

import com.HotelManagement.CustomerService.Entity.CustomerEntity;
import com.HotelManagement.CustomerService.Entity.LoginRequest;
import com.HotelManagement.CustomerService.Entity.LoginResponse;
import com.HotelManagement.CustomerService.Entity.ResetPasswordRequest;
import com.HotelManagement.CustomerService.Exception.CustomerNotFoundException;
import com.HotelManagement.CustomerService.Exception.InvalidCredentialsException;
import com.HotelManagement.CustomerService.Repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = new CustomerEntity();
        customer.setCustomerId(1L);
        customer.setUsername("rajesh123");
        customer.setEmail("rajesh@example.com");
        customer.setPhoneNumber(1234567890L);
        customer.setPassword("plainPassword");
    }

    @Test
    void testRegisterCustomer() {
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        CustomerEntity saved = customerService.registerCustomer(customer);
        assertNotNull(saved);
        assertEquals("rajesh123", saved.getUsername());
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    void testLogin_Success() {
        when(customerRepository.findByUsername("rajesh123")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("plainPassword", customer.getPassword())).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest("rajesh123", "plainPassword");
        LoginResponse loginResponse = customerService.login(loginRequest);

        assertNotNull(loginResponse);
        assertEquals("rajesh123", loginResponse.getCustomer().getUsername());
        assertEquals("Customer login successfully with username and password", loginResponse.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        when(customerRepository.findByUsername("rajesh123")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrongPassword", customer.getPassword())).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest("rajesh123", "wrongPassword");

        assertThrows(InvalidCredentialsException.class, () -> customerService.login(loginRequest));
    }

    @Test
    void testLogin_UserNotFound() {
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(customerRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest("unknown", "password");

        assertThrows(InvalidCredentialsException.class, () -> customerService.login(loginRequest));
    }

    @Test
    void testResetPassword_Success() {
        when(customerRepository.findByEmailIgnoreCase("rajesh@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded-password");
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("rajesh@example.com");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        customerService.resetPassword(request);

        assertEquals("encoded-password", customer.getPassword());
        verify(customerRepository).save(customer);
    }

    @Test
    void testResetPassword_PasswordsDoNotMatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("rajesh@example.com");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("differentPassword");

        assertThrows(IllegalArgumentException.class, () -> customerService.resetPassword(request));
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    void testResetPassword_EmailNotFound() {
        when(customerRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("unknown@example.com");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        assertThrows(CustomerNotFoundException.class, () -> customerService.resetPassword(request));
    }

    @Test
    void testGetAllCustomers() {
        List<CustomerEntity> customers = List.of(customer);
        when(customerRepository.findAll()).thenReturn(customers);

        List<CustomerEntity> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void testUpdateCustomer_Success() {
        CustomerEntity updated = new CustomerEntity();
        updated.setUsername("UpdatedName");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customer);

        customerService.updateCustomer(1L, updated);

        assertEquals("UpdatedName", customer.getUsername());
        verify(customerRepository).save(customer);
    }

    @Test
    void testUpdateCustomer_NotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.updateCustomer(1L, customer);
        });
    }

    @Test
    void testDeleteCustomer_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void testDeleteCustomer_NotFound() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer(1L);
        });
    }
}
