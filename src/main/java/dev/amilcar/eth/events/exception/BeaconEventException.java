package dev.amilcar.eth.events.exception;

/**
 * Exception thrown when an error occurs while working with Ethereum Beacon Chain events.
 */
public class BeaconEventException extends RuntimeException {
    
    /**
     * Creates a new BeaconEventException with the specified message.
     *
     * @param message the detail message
     */
    public BeaconEventException(String message) {
        super(message);
    }
    
    /**
     * Creates a new BeaconEventException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BeaconEventException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new BeaconEventException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public BeaconEventException(Throwable cause) {
        super(cause);
    }
}