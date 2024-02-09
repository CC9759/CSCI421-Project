package storageManager;

import java.util.ArrayList;
import java.util.Arrays;

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

    public int getPageId() {
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

    /* 
     * not sure if this is where it goes
     * func to read in page
     * assumes record header is 
     * 1 byte for # of entires
     * 2 byte for location and size -> 2 * N size
     * k byte for each i location
     */ 
    public ArrayList<Record> readPage(byte[] bytes) {
        int num_of_entries = bytes[0];
        ArrayList<Record> recs = new ArrayList<Record>();

        for(int i = 1; i < num_of_entries*2; i+=2) {
            int size = bytes[i];
            int location = bytes[i+1];

            // now has bytes of record
            byte[] record_bytes = Arrays.copyOfRange(bytes, location, size);
            
            // call function to read record

            // add record to list
        }
        return recs;
    }


}
