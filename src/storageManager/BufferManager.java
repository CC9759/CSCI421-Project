package storageManager;

import Exceptions.NoTableException;

import java.util.ArrayList;
import java.util.HashMap;

public class BufferManager {

    private ArrayList<Page> buffer;
    private final int bufferSize;

    public BufferManager(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new ArrayList<Page>(bufferSize);
    }

    public Page getPage(Table table, int pageNumber) {
        for (Page page : buffer) {
            if (page.getPageTableId() == table.schema.getTableId() && page.getPageId() == pageNumber) {
                return page;
            }
        }
        // If not in the buffer
        Page page = table.readPage(pageNumber);
        if (page != null) {
            addToBuffer(table, page);
        }
        return page;
    }

    public void insertRecord(Page insertPage, Record record, int index) {
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
            if (removedPage.hasBeenUpdated()) {
                table.writePage(removedPage);
            }
        }
        page.touch();
        this.buffer.add(page);
    }

    public int getLRUPageIndex() {
        long oldestTime = Long.MAX_VALUE;
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

    public void flush(HashMap<Integer, Table> idToTable) {
        for (Page page : this.buffer) {
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
                Record deleted = page.deleteRecord(recordIndex);
                if (page.getRecords().size() == 0) {
                    handleEmptyPageRemoval(table, page);
                }
                return deleted;
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
     * @param table       table the page split
     * @param removedPageId id of the new page
     * @throws NoTableException No table of tableId
     */
    public void updatePageNumbers(Table table, int removedPageId, int change) {
        if (change > 0) {
            // start from the end to avoid duplicate IDs in buffer at one time
            for (int i = table.getNumPages() - 1; i >= removedPageId; i--) {
                getPage(table, i).updatePageNumber(change);
            }
        } else {
            // start from the beginning to avoid duplicate IDs in buffer at one time
            for (int i = removedPageId + 1; i < table.getNumPages(); i++) {
                getPage(table, i).updatePageNumber(change);
            }
        }

        table.updatePageCount(change);
    }

    private int getIndexInBuffer(int pageNumber) {
        for (int i = 0; i < this.buffer.size(); i++) {
            if (buffer.get(i).getPageId() == pageNumber) {
                return i;
            }
        }
        return -1;
    }

    private void handleEmptyPageRemoval(Table table, Page page) throws NoTableException {
        int toRemove = this.getIndexInBuffer(page.getPageId());
        if (toRemove != -1) {
            this.buffer.remove(toRemove);
        }
        updatePageNumbers(table, page.getPageId(), -1);
    }
}
