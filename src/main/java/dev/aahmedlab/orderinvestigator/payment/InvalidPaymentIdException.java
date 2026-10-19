package dev.aahmedlab.orderinvestigator.payment;

public class InvalidPaymentIdException extends RuntimeException {
    public InvalidPaymentIdException(String message){
        super(message);
    }

    public InvalidPaymentIdException(String message, Throwable cause){
        super(message, cause);
    }

}

