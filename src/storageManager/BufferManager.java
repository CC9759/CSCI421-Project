package storageManager;

import Exceptions.NoTableException;

import java.util.ArrayList;
import java.util.HashMap;

public class BufferManager {
    
    private ArrayList<Page> buffer;
    private int bufferSize;


    public BufferManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new ArrayList<Page>(bufferSize);
    }

    public Page getPage(Table table, int pageNumber) {
        for (Page page: buffer) {
            if (page.getPageTableId() == table.schema.getTableId() && page.getPageId() == pageNumber) {
                return page;
            }
        }
        // If not in the buffer
        Page page = table.readPage(pageNumber);
        addToBuffer(table, page);
        return page;
    }

    public void insertRecord(Page insertPage, Record record, int index){
        try {
            insertPage.insertRecord(record, index);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void addToBuffer(Table table, Page page) {
        if (buffer.size() == this.bufferSize) {
            int leastUsedPageIndex = this.getLRUPageIndex();
            Page removedPage = buffer.remove(leastUsedPageIndex);
            table.writePage(removedPage);
        }
        page.touch();
        this.buffer.add(page);
    }

    public int getLRUPageIndex() {
        long oldestTime = -1;
        int oldestIndex = 0;
        for (int i = 0; i < this.buffer.size(); i++) {
            long lastUpdated = this.buffer.get(i).getUpdatedAt();
            if (lastUpdated < oldestTime) {
                oldestTime = lastUpdated;
                oldestIndex = i;
            }
        }
        return oldestIndex;
    }

    public void flush(HashMap<Integer, Table> idToTable){
        for (Page page: this.buffer) {
            if (page.hasBeenUpdated()) {
                idToTable.get(page.getPageTableId()).writePage(page);
            }
        }
        this.buffer = new ArrayList<Page>();
    }

    public Record deleteRecord(Table table, Attribute primaryKey) throws NoTableException {
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = this.getPage(table, i);
            int recordIndex = page.getRecordByKey(primaryKey);
            if (recordIndex != -1) {
                page.deleteRecord(recordIndex);
                if (page.getRecords().size() == 0) {
                    updatePageNumbers(table, page.getPageId(), -1);
                }
            }
        }
        return null;
    }

    public Record updateRecord(Table table, Record record) {
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = this.getPage(table, i);
            int recordIndex = page.getRecordByKey(record.getPrimaryKey());
            if (recordIndex != -1) {
                page.updateRecord(recordIndex, record);
            }
        }
        return null;
    }

    public Record getRecordByPrimaryKey(Table table, Attribute primaryKey) {
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = this.getPage(table, i);
            int recordIndex = page.getRecordByKey(primaryKey);
            if (recordIndex != -1) {
                return page.getRecords().get(recordIndex);
            }
        }
        return null;
    }

    /**
     * Update page number/ids after a page split
     * @param table table the page split
     * @param splitPageId id of the new page
     * @throws NoTableException No table of tableId
     */
    public void updatePageNumbers(Table table, int splitPageId, int change) throws NoTableException {
        for (int i = splitPageId; i < table.getNumPages(); i++) {
            Page page = getPage(table, i);
            page.updatePageNumber(change);
        }

        table.updatePageCount(change);
    }






    // public Record getRecordByPrimaryKey(Object primaryKey);

    // public ArrayList<Record> getAllRecords(int tableNumber);

    // public void deleteRecord(Object PrimaryKey);
    // public void updateRecord(Object primaryKey, Record record);
    // public storageManager.Page splitPage(storageManager.Page page);
    // public storageManager.Page createPage(int tableId, Record[] records);

}
