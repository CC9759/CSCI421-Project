package storageManager;

import java.util.ArrayList;
import java.util.HashMap;

import Exceptions.NoTableException;
import Exceptions.PageOverfullException;
import catalog.TableSchema;
import catalog.Catalog;


public class StorageManager {

    private BufferManager bufferManager;
    private static StorageManager storageManager;
    private HashMap<Integer, Table> idToTable;

    public StorageManager(int bufferSize) {
        if (storageManager == null) {
            this.bufferManager = new BufferManager(bufferSize);
            this.idToTable = new HashMap<>();
            this.readTableData();
        }
    }

    public static void InitStorageManager(int bufferSize) {
        if (storageManager == null) {
            storageManager = new StorageManager(bufferSize);
        }
    }
    public static StorageManager GetStorageManager() {
        return storageManager;
    }

    public Page getPage(int tableNumber, int pageNumber) throws NoTableException {
        Table table = ensureTable(tableNumber);
        return bufferManager.getPage(table, pageNumber);
    }

    public void insertRecord(int tableId, Record record) throws PageOverfullException, NoTableException {
        Table table = ensureTable(tableId);
        if (table.getNumPages() == 0) {
            Page newPage = table.createPage();
            bufferManager.addToBuffer(table, newPage);
            bufferManager.insertRecord(newPage, record, 0);
            return;
        }
    
        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = getPage(tableId, i);
            int insertPos = findInsertPosition(page, record);
            // Case where this records primary key is larger than all the others
            if (insertPos == -1 && i == table.getNumPages() - 1) {
                insertPos = page.getRecords().size();
            }
            if (insertPos != -1) {
                if (page.canInsertRecord(record)) {
                    bufferManager.insertRecord(page, record, insertPos);
                    return;
                } else {
                    Page newPage = page.splitPage();
                    bufferManager.updatePageNumbers(table, newPage.getPageId(), 1);
                    bufferManager.addToBuffer(table, newPage);
                    insertPos = findInsertPosition(page, record);
                    if (insertPos == -1) {
                        insertPos = findInsertPosition(newPage, record);
                        if (insertPos == -1) {
                            insertPos = newPage.getRecords().size();
                        }
                        bufferManager.insertRecord(newPage, record, insertPos);
                    } else {
                        bufferManager.insertRecord(page, record, insertPos);
                    }
                    
                    return;
                }
            }
        }
    }

    public Record getRecordByPrimaryKey(int tableId, Attribute primaryKey) throws NoTableException {
        Table table = ensureTable(tableId);
        return this.bufferManager.getRecordByPrimaryKey(table, primaryKey);

    }
    public ArrayList<Record> getAllRecords(int tableNumber) throws NoTableException {
        Table table = ensureTable(tableNumber);
        ArrayList<Record> result = new ArrayList<>();
        for (int i = 0; i < table.getNumPages(); i++) {
            result.addAll(bufferManager.getPage(table, i).getRecords());
        }

        return result;
    }

    public Record deleteRecord(int tableId, Attribute primaryKey) throws NoTableException {
        Table table = ensureTable(tableId);
        return this.bufferManager.deleteRecord(table, primaryKey);
    }
    public Record updateRecord(int tableId, Record record) throws NoTableException {
        Table table = ensureTable(tableId);
        return this.bufferManager.updateRecord(table, record);
    }

    /**
     * Get table schema data
     */
    private void readTableData() {
        ArrayList<TableSchema> tableSchemas = Catalog.getCatalog().getTableSchema();
        for (TableSchema schema: tableSchemas) {
            this.idToTable.put(schema.getTableId(), new Table(schema));
        }
    }

    /**
     * Fetch a table
     * @param tableId table id
     * @return Table of tableId
     * @throws NoTableException no table of tableId
     */
    private Table ensureTable(int tableId) throws NoTableException {
        Table table = this.idToTable.get(tableId);
        if (table == null) {
            throw new NoTableException(tableId);
        }
        return table;
    }

    public void flushBuffer() {
        this.bufferManager.flush(this.idToTable);
    }

    private int findInsertPosition(Page page, Record record) {
        ArrayList<Record> records = page.getRecords();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).compareTo(record) > 0) {
                return i;
            }
        }

        return -1;
    }
}
