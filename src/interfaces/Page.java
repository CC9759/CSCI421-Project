package interfaces;

import java.util.ArrayList;

public interface Page {

    public int getPageId();
    public TableSchema getPageTable();
    public ArrayList<Record> getRecords();
    public Record getRecordById(int id);
    public void writeToMemory();

}
