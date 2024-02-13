package storageManager;

import Exceptions.PageOverfullException;

import java.util.ArrayList;
import java.util.Arrays;


public class Page {
    private int tableId;
    private int pageId;
    private ArrayList<Record> records;
    private int freeSpaceAmount;
    private int pageSize;
    private long updatedAt; // Used by BufferManager to sort LRU

    // indicate to the buffer manager whether this page was modified and needs to be written to memory
    private boolean wasUpdated;

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

    public void insertRecord(Record record, int index) throws PageOverfullException {
        if (!canInsertRecord(record)) {
            throw new PageOverfullException(pageId);
        }
        records.add(index, record);
        this.updateFreeSpace();
    }

    public Page splitPage() throws PageOverfullException {
        int middle = records.size() / 2;
        // Slice the second half of the records in the page
        ArrayList<Record> newPageRecords = new ArrayList<>(records.subList(middle, records.size()));
        records.subList(middle, records.size()).clear();
        this.updateFreeSpace();

        // Insert the second half of the records into the new page
        Page newPage = new Page(this.tableId, this.pageId + 1, this.pageSize);
        for (Record record : newPageRecords) {
            newPage.insertRecord(record, newPage.records.size());
        }
        newPage.updateFreeSpace();

        return newPage;
    }

    private void updateFreeSpace() {
        int usedSpace = 0;
        for (Record record : records) {
            usedSpace += record.getSize();
        }
        usedSpace += records.size() * 4; // 4 byte pointer for each record
        this.wasUpdated = true;
        this.touch();
        this.freeSpaceAmount = this.pageSize - usedSpace;
    }

    public void incrementPageNumber() {
        this.pageId++;
        this.wasUpdated = true;
        this.touch();
    }

    public boolean hasBeenUpdated() {
        return this.wasUpdated;
    }

    /* 
     * not sure if this is where it goes
     * func to read in page
     * assumes record header is 
     * 1 byte for # of entires
     * 2 byte for location and size -> 2 * N size
     * k byte for each i location
     */ 
    public ArrayList<Record> readPage() {
        // use this.tableId, pagesize, page id
        //String location = this.schema.getLocation; this method needs to exist
//        int num_of_entries = bytes[0];
//        ArrayList<Record> recs = new ArrayList<Record>();
//
//        for(int i = 1; i < num_of_entries*2; i+=2) {
//            int size = bytes[i];
//            int location = bytes[i+1];

            // now has bytes of record
            //byte[] record_bytes = Arrays.copyOfRange(bytes, location, size);

            // call function to read record

            // add record to list
        //}
        //return recs;
        return null;
    }


}
