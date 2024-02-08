package interfaces;

import java.util.ArrayList;

public interface BufferManager {

    // These are all called by storage manager
    public Record getRecordByPrimaryKey(Object primaryKey);
    public Page getPage(int tableNumber, int pageNumber);
    public ArrayList<Record> getAllRecords(int tableNumber);
    public void insertRecord(Record record);
    public void deleteRecord(Object PrimaryKey);
    public void updateRecord(Object primaryKey, Record record);

    
    public Page splitPage(Page page);
    public void flush(); // on program shutdown
    public Page getLRUPage();
    public Page createPage(int tableId, Record[] records);



}
