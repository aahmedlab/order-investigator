package dev.aahmedlab.orderinvestigator.payment;

public class PaymentLookupUnavailableException extends RuntimeException {
    public PaymentLookupUnavailableException(String message){
        super(message);
    }

    public PaymentLookupUnavailableException(String message, Throwable cause){
        super(message, cause);
    }

}
