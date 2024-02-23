package Exceptions;

public class InvalidTypeException extends Exception {
        public InvalidTypeException(String stringType) {
                super("The creation of type {" + stringType + "} is not possible, this type does not exist.\n" +
                        "Allowed types are INT, DOUBLE, BOOLEAN, CHAR(x), VARCHAR(X).");
        }
}