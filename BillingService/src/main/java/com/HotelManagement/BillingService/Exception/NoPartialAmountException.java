package com.HotelManagement.BillingService.Exception;

public class NoPartialAmountException extends RuntimeException{
    public NoPartialAmountException(String message){
        super(message);
    }
}
