package storageManager;

import java.util.ArrayList;
import interfaces.Record;

public class Page {
    private int tableId;
    private int pageId;
    private ArrayList<Record> records;
    private int freeSpaceAmount;
    private int pageSize;
    private long updatedAt; // Used by BufferManager

    public Page(int tableId, int pageId, int pageSize) {
        this.tableId = tableId;
        this.pageSize = pageSize;
        this.pageId = pageId;
        this.freeSpaceAmount = pageSize;
        this.records = new ArrayList<Record>();
    }

    public int getPageId(){
        return this.pageId;
    }

    public int getPageTableId(){
        return this.tableId;
    }

    public long getUpdatedAt() {
        return this.updatedAt;
    }
    // Updates the pages last update time
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    public ArrayList<Record> getRecords(){
        return this.records;
    }
    public Record getRecordById(int id) {
        return null;
    }
    public void writeToMemory(){
        
    }

    public boolean canInsertRecord(Record record) {
        // +4 is space needed for its file pointer
        return this.freeSpaceAmount > record.getSize() + 4;
    }

    public void insertRecord(Record record) throws Exception {
        if (!canInsertRecord(record)) {
            throw new Exception("no");
        }
        records.add(record);
        this.touch();
    }
}
