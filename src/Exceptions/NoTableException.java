package Exceptions;

public class NoTableException extends Exception {
    public NoTableException(int tableId) {
        super("Table " + tableId + " access attempted but does not exist");
    }

    public NoTableException(String tableName) {
        super("Table " + tableName + " access attempted but does not exist");
    }
}
