package storageManager;

import java.util.ArrayList;
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
        addToBuffer(page);
        return page;
    }

    public void insertRecord(Page insertPage, Record record, int index){
        try {
            insertPage.insertRecord(record, index);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void addToBuffer(Page page) {
        if (buffer.size() == this.bufferSize) {
            int leastUsedPageIndex = this.getLRUPageIndex();
            Page removedPage = buffer.remove(leastUsedPageIndex);
            removedPage.writeToMemory();
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

    public void flush(){
        for (Page page: this.buffer) {
            if (page.hasBeenUpdated()) {
                page.writeToMemory();
            }
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
