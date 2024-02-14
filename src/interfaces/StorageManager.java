package interfaces;

import java.util.ArrayList;

public interface StorageManager {

    public Record getRecordByPrimaryKey(int tableId, Attribute primaryKey);
    public Page getPage(int tableNumber, int pageNumber);
    public ArrayList<Record> getAllRecords(int tableNumber);
    public void insertRecord(int tableId, Record record);
    public void deleteRecord(Object PrimaryKey);
    public void updateRecord(Record record);
}
