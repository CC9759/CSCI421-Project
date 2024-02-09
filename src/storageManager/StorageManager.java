package storageManager;

import java.util.ArrayList;
import interfaces.Record;

public class StorageManager {

    private BufferManager bufferManager;

    public StorageManager() {
        this.bufferManager = new BufferManager(128); // this will be defined by user later
    }

    public Record getRecordByPrimaryKey(Object primaryKey){
        return null;
    }
    public Page getPage(int tableNumber, int pageNumber){
        return null;
    }
    public ArrayList<Record> getAllRecords(int tableNumber){
        return null;
    }
    public void insertRecord(int tableId, Record record){
        bufferManager.insertRecord(tableId, record);
    }
    public void deleteRecord(Object PrimaryKey){}
    public void updateRecord(Object primaryKey, Record record){}
}
