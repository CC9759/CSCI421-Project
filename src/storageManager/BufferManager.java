package storageManager;

import java.util.ArrayList;
import interfaces.Record;
public class BufferManager {
    
    private ArrayList<Page> buffer;
    private int bufferSize;


    public BufferManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new ArrayList<Page>(bufferSize);
    }

    public Page getPage(int tableNumber, int pageNumber) {
        for (Page page: this.buffer) {
            if (page.getPageTableId() == tableNumber && page.getPageId() == pageNumber) {
                return page;
            }
        }
        return null;
    }

    public void insertRecord(int tableId, Record record){
        Page insertPage = this.getPageToInsert(tableId, record);
        if (insertPage == null) {
            // int newPageId = Catalog.getTableByid(tableId).getNumPages() + 1
            // Catalog.getTableByid(tableId).incrementNumPages()
            int newPageId = 0; // Get 1 + number of current pages (to be implemented from table)
            insertPage = new Page(tableId, 0, newPageId);
            this.addToBuffer(insertPage);
        }
        try {
            insertPage.insertRecord(record);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public Page getPageToInsert(int tableId, Record record) {
        for (Page page : this.buffer) {
            if (page.getPageTableId() == tableId && page.canInsertRecord(record)) {
                return page;
            }
        }
        return null;
    }

    public void addToBuffer(Page page) {
        if (buffer.size() == this.bufferSize) {
            int leastUsedPageIndex = this.getLRUPageIndex();
            Page removedPage = buffer.remove(leastUsedPageIndex);
            removedPage.writeToMemory();
        }
        this.buffer.add(page);
        page.touch();
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

    public void flush(){
        for (Page page: this.buffer) {
            page.writeToMemory();
        }
        this.buffer = new ArrayList<Page>();
    }


    // public Record getRecordByPrimaryKey(Object primaryKey);

    // public ArrayList<Record> getAllRecords(int tableNumber);

    // public void deleteRecord(Object PrimaryKey);
    // public void updateRecord(Object primaryKey, Record record);
    // public storageManager.Page splitPage(storageManager.Page page);
    // public storageManager.Page createPage(int tableId, Record[] records);

}
