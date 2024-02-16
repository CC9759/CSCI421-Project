package Exceptions;

public class InsufficientArgumentException extends Exception {
        public InsufficientArgumentException(String funcString) {
                super("The function " + funcString + " doesn't have the required number of arguments.");
        }
}
