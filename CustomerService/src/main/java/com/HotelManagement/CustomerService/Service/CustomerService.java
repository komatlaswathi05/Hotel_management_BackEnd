package com.HotelManagement.CustomerService.Service;

import com.HotelManagement.CustomerService.Entity.CustomerEntity;
import com.HotelManagement.CustomerService.Entity.LoginRequest;
import com.HotelManagement.CustomerService.Entity.LoginResponse;
import com.HotelManagement.CustomerService.Entity.ResetPasswordRequest;
import com.HotelManagement.CustomerService.Entity.MessageResponse;

import java.util.List;

public interface CustomerService {

    CustomerEntity registerCustomer(CustomerEntity customerEntity);

    LoginResponse login(LoginRequest loginRequest);

    MessageResponse resetPassword(ResetPasswordRequest request);

    List<CustomerEntity> getAllCustomers();

    CustomerEntity getCustomerById(long customerId);

    void updateCustomer(long customerId,CustomerEntity newCustomerEntity);

    void deleteCustomer(Long customerId);
}
