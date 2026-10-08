package com.HotelManagement.BillingService.Exception;

public class BillAlreadyGeneratedException extends RuntimeException{
    public BillAlreadyGeneratedException(String message){
        super(message);
    }
}
