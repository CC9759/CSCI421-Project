package interfaces;

import java.util.ArrayList;

public interface StorageManager {

    public Record getRecordByPrimaryKey(Object primaryKey);
    public Page getPage(int tableNumber, int pageNumber);
    public ArrayList<Record> getAllRecords(int tableNumber);
    public void insertRecord(Record record);
    public void deleteRecord(Object PrimaryKey);
    public void updateRecord(Object primaryKey, Record record);
}
