package storageManager;

import Exceptions.PageOverfullException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Page {
    private final int tableId;
    private int pageId;
    private final ArrayList<Record> records;
    private int freeSpaceAmount;
    private final int pageSize;
    private long updatedAt; // Used by BufferManager to sort LRU

    // indicate to the buffer manager whether this page was modified and needs to be written to memory
    private boolean wasUpdated;

    public Page(int tableId, int pageId, int pageSize, ArrayList<Record> records) {
        this.tableId = tableId;
        this.pageSize = pageSize;
        this.pageId = pageId;
        this.records = records;
        calculateFreeSpace();
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

    public int getRecordByKey(Attribute primaryKey) {
        int numRecordsInPage = records.size();

        if (numRecordsInPage == 0) {
            return -1;
        }
        // Check if the primary key is on this page
        if (primaryKey.compareTo(records.get(numRecordsInPage - 1).getPrimaryKey()) <= 0) {
            int left = 0;
            int right = numRecordsInPage - 1;
            while (left <= right) {
                int middle = left + (right - left) / 2;
                Attribute middleRecordKey = records.get(middle).getPrimaryKey();
                if (primaryKey.compareTo(middleRecordKey) == 0) {
                    return middle; // Record found
                } else if (primaryKey.compareTo(middleRecordKey) < 0) {
                    right = middle - 1;
                } else {
                    left = middle + 1;
                }
            }
        }
        return -1;
    }

    public boolean canInsertRecord(Record record) {
        return this.freeSpaceAmount >= (record.getSizeFile() + 8); // include record slot
    }

    public void insertRecord(Record record, int index) throws PageOverfullException {
        if (!canInsertRecord(record)) {
            throw new PageOverfullException(pageId);
        }
        records.add(index, record);
        this.update();
    }

    public Page splitPage() throws PageOverfullException {
        int middle = records.size() / 2;
        // Slice the second half of the records in the page
        ArrayList<Record> newPageRecords = new ArrayList<>(records.subList(middle, records.size()));
        records.subList(middle, records.size()).clear();
        this.update();

        // Insert the second half of the records into the new page
        Page newPage = new Page(this.tableId, this.pageId + 1, this.pageSize, newPageRecords);
        newPage.update();
        return newPage;
    }

    private void calculateFreeSpace() {
        int usedSpace = 0;
        usedSpace += getHeaderSize();
        for (Record record : records) {
            usedSpace += record.getSizeFile();
        }
        this.freeSpaceAmount = this.pageSize - usedSpace;
    }

    private void update() {
        this.wasUpdated = true;
        this.touch();
        calculateFreeSpace();
    }

    public void updatePageNumber(int change) {
        this.pageId += change;
        this.wasUpdated = true;
        this.touch();
    }

    public boolean hasBeenUpdated() {
        return this.wasUpdated;
    }

    public Record deleteRecord(int index) {
        Record result = this.records.remove(index);
        this.update();
        return result;
    }


    public int getFreeSpaceAmount() {
        return this.freeSpaceAmount;
    }

    public int getHeaderSize() {
        // Page ID | numberofslots | end of free space | slots (position | length)[]
        return 4 + 4 + 4 + (8 * records.size());
    }

    public byte[] serializePage() throws IOException {
        ArrayList<Record> records = getRecords();
        if (records.size() == 0) {
            System.out.println("Fatal: trying to write a page of size 0 to memory " + pageId);
            System.exit(1);
        }
        int freeSpace = getFreeSpaceAmount();
        int numberOfSlots = records.size();
        int headerSize = getHeaderSize();
        int endOfFreeSpace = headerSize + freeSpace;
        int[] recordPositions = new int[numberOfSlots];
        int[] recordSizes = new int[numberOfSlots];

        int cumulativeRecordSize = 0;
        ByteArrayOutputStream baosRecords = new ByteArrayOutputStream();
        DataOutputStream dosRecords = new DataOutputStream(baosRecords);
        for (int i = 0; i < records.size(); i++) {
            var record = records.get(i);
            int startSize = baosRecords.size();
            record.serialize(dosRecords);
            dosRecords.flush();
            int endSize = baosRecords.size();
            recordSizes[i] = endSize - startSize;
            if (recordSizes[i] != record.getSizeFile()) {
                System.out.println("Fatal: Calculated record size is different than actual");
                System.exit(1);
            }
            recordPositions[i] = endOfFreeSpace + cumulativeRecordSize;
            cumulativeRecordSize += recordSizes[i];
        }
        dosRecords.flush();
        byte[] recordData = baosRecords.toByteArray();

        ByteArrayOutputStream pageBaos = new ByteArrayOutputStream();
        DataOutputStream pageDos = new DataOutputStream(pageBaos);

        pageDos.writeInt(getPageId());
        pageDos.writeInt(numberOfSlots);
        pageDos.writeInt(endOfFreeSpace);
        for (int i = 0; i < numberOfSlots; i++) {
            pageDos.writeInt(recordPositions[i]);
            pageDos.writeInt(recordSizes[i]);
        }
        for (int i = 0; i < freeSpace; i++) {
            pageDos.writeByte(0);
        }
        pageDos.write(recordData);
        pageDos.flush();
        return pageBaos.toByteArray();
    }
}
