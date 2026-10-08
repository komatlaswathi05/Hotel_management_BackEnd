package com.HotelManagement.BillingService.Exception;

public class BillNotFoundException extends RuntimeException{
    public BillNotFoundException(String message){
        super(message);
    }
}
