package com.HotelManagement.BillingService.Exception;

public class PaymentAlreadyDoneException extends RuntimeException{
    public PaymentAlreadyDoneException(String message){
        super(message);
    }
}
