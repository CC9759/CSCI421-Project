package interfaces;

import storageManager.Attribute;

import java.util.ArrayList;

public interface Page {

    public int getPageId();
    public TableSchema getPageTable();
    public ArrayList<Record> getRecords();
    public int getRecordByKey(Attribute primaryKey);
    public void writeToMemory();

}
