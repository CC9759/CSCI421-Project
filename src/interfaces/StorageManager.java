package interfaces;

import Exceptions.NoTableException;

import java.util.ArrayList;

public interface StorageManager {

    public Record getRecordByPrimaryKey(int tableId, Attribute primaryKey);
    public Page getPage(int tableNumber, int pageNumber);
    public ArrayList<Record> getAllRecords(int tableNumber);
    public void insertRecord(int tableId, Record record);
    public Record deleteRecord(int tableId, Attribute primaryKey);
    public Record updateRecord(int tableId, Record record);
}
