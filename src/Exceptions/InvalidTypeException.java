package Exceptions;

public class InvalidTypeException extends Exception {
        public InvalidTypeException(String stringType) {
                super("The creation of type {" + stringType + "} is not possible, this type does not exist");
        }
}