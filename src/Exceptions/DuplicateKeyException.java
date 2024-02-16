package Exceptions;

import storageManager.Attribute;

public class DuplicateKeyException extends Exception{
    public DuplicateKeyException(Attribute key) {
        super("Record with attribute " + key.getData().toString() + " already exists.");
    }
}
