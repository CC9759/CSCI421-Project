package Exceptions;

public class IllegalOperationException extends Exception {
    public IllegalOperationException(String message) {
        super("Illegal Operation: " + message);
    }
}
