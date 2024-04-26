/**
 * Storage Manager
 * Handles all CRUD operations
 * @author Daniel Tregea
 */
package storageManager;

import java.util.ArrayList;
import java.util.HashMap;

import BPlusTree.Index;
import BPlusTree.TreeNode;
import Exceptions.DuplicateKeyException;
import Exceptions.IllegalOperationException;
import Exceptions.NoTableException;
import Exceptions.PageOverfullException;
import catalog.TableSchema;
import catalog.Catalog;


public class StorageManager {

    private BufferManager bufferManager;
    private static StorageManager storageManager;
    private HashMap<Integer, Table> idToTable;

    // for debugging
    private boolean verbose = false;

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

        if (verbose) 
            System.out.println("Reading Page Number: " + Integer.toString(pageNumber));

        Table table = ensureTable(tableNumber);
        return bufferManager.getPage(table, pageNumber);
    }

    public void insertRecord(int tableId, Record record) throws PageOverfullException, NoTableException, DuplicateKeyException, IllegalOperationException {
        Table table = ensureTable(tableId);
        if (table.getNumPages() == 0) {
            Page newPage = table.createPage();
            bufferManager.addToBuffer(table, newPage);
            bufferManager.insertRecord(table, newPage, record, 0);
            return;
        }
        if (Catalog.getCatalog().getIndexing()) {
            var existing = getRecordByPrimaryKey(tableId, record.getPrimaryKey());
            if (existing != null) throw new DuplicateKeyException(record.getPrimaryKey());
            // Find node it should be inserted in. since it doesnt exist, index.pagenumber will be a node number
            Index index = table.findIndex(record.getPrimaryKey().getData());
            int insertPage = findInsertPosition(table, index.pageNumber, record);
            if (insertPage == -1) { // largest value found,
                insertPage = table.getNumPages() - 1;
            }
            Page page = bufferManager.getPage(table, insertPage);
            int insertPos = findInsertPosition(page, record);
            if (insertPos == -1 && page.getPageId() == table.getNumPages() - 1) { // page id is the node number
                insertPos = page.getRecords().size();
            }
            handleSplit(record, table, page, insertPos);
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
                handleSplit(record, table, page, insertPos);
                return;
            }
        }

    }

    private void handleSplit(Record record, Table table, Page page, int insertPos) throws PageOverfullException, IllegalOperationException, DuplicateKeyException {
        if (page.canInsertRecord(record)) {
            bufferManager.insertRecord(table, page, record, insertPos);
        } else {
            Page newPage = page.splitPage(table);
            bufferManager.updatePageNumbers(table, newPage.getPageId(), 1);
            bufferManager.addToBuffer(table, newPage);
            insertPos = findInsertPosition(page, record);
            if (insertPos == -1) {
                insertPos = findInsertPosition(newPage, record);
                if (insertPos == -1) {
                    insertPos = newPage.getRecords().size();
                }
                bufferManager.insertRecord(table, newPage, record, insertPos);
            } else {
                bufferManager.insertRecord(table, page, record, insertPos);
            }
        }
    }

    public Record getRecordByPrimaryKey(int tableId, Attribute primaryKey) throws NoTableException, IllegalOperationException {
        Table table = ensureTable(tableId);

        if (Catalog.getCatalog().getIndexing()) {
            Index index = table.findIndex(primaryKey.getData());
            if (index.recordPointer == -1) { // not found
                return null;
            }
            Page page = this.bufferManager.getPage(table, index.pageNumber);
            return page.getRecords().get(index.recordPointer);
        }
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

    public Record deleteRecord(int tableId, Attribute primaryKey) throws NoTableException, IllegalOperationException {
        Table table = ensureTable(tableId);
        Index index = null;
        if (Catalog.getCatalog().getIndexing()) {
            var record = getRecordByPrimaryKey(tableId, primaryKey);
            if (record == null) {
                return null;
            }
            // Since the record exists, index will be a page number
            index = table.findIndex(primaryKey.getData());
        }
        return this.bufferManager.deleteRecord(table, primaryKey, index);
    }
    public void updateRecord(int tableId, Record record) throws NoTableException, PageOverfullException, DuplicateKeyException, IllegalOperationException {
        deleteRecord(tableId, record.getPrimaryKey());
        insertRecord(tableId, record);
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
    public Table ensureTable(int tableId) throws NoTableException {
        Table table = this.idToTable.get(tableId);
        if (table == null) {
            TableSchema newTableSchema = Catalog.getCatalog().getTableSchema(tableId);
            if (newTableSchema == null) {
                throw new NoTableException(tableId);
            } else {
                table = new Table(newTableSchema);
                this.idToTable.put(newTableSchema.getTableId(), table);
            }
        }
        return table;
    }

    public void flushBuffer() {
        this.bufferManager.flush(this.idToTable);
    }

    private int findInsertPosition(Table table, int nodeNumber, Record record) throws DuplicateKeyException, IllegalOperationException {
        TreeNode node = table.readNode(nodeNumber);
        var sks = node.getSearchKeys();
        for (int i = 0; i < sks.size(); i++) {
            if (sks.get(i).compareTo(record.getPrimaryKey()) > 0) {
                return node.getIndices().get(i).pageNumber;
            }
        }
        return -1;
    }

    private int findInsertPosition(Page page, Record record) throws DuplicateKeyException {
        ArrayList<Record> records = page.getRecords();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).compareTo(record) > 0) {
                return i;
            } else if (records.get(i).compareTo(record) == 0) {
                throw new DuplicateKeyException(record.getPrimaryKey());
            }
        }

        return -1;
    }

    // For test class use only
    public HashMap<Integer, Table> getIdToTable() {
        return idToTable;
    }

    public boolean turnOnIndexing() {
        Catalog.getCatalog().setIndexing(true);
        try {
            for (Table table: idToTable.values()) {
                for (int i = 0; i < table.getNumPages(); i++) {
                    Page page = getPage(table.schema.getTableId(), i);
                    var records = page.getRecords();
                    for (int j = 0; j < records.size(); j++) {
                        table.insertNode(records.get(j).getPrimaryKey(), new Index(page.getPageId(), j));
                    }
                }
            }
        } catch (NoTableException | IllegalOperationException nte) {
            System.err.println(nte.getMessage());
            return false;
        }
        return true;
    }
}
