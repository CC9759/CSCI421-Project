package storageManager;

import java.util.ArrayList;
import java.util.HashMap;

import Exceptions.NoTableException;
import Exceptions.PageOverfullException;
import catalog.TableSchema;
import catalog.Catalog;


public class StorageManager {

    private final BufferManager bufferManager;
    private HashMap<Integer, Table> idToTable;

    public StorageManager() {
        this.bufferManager = new BufferManager(128); // this will be defined by user later
        this.readTableData();
    }

    private Page getPage(int tableNumber, int pageNumber) throws NoTableException {
        Table table = ensureTable(tableNumber);
        return bufferManager.getPage(table, pageNumber);
    }

    public void insertRecord(int tableId, Record record) throws PageOverfullException, NoTableException {
        Table table = ensureTable(tableId);
        if (table.getNumPages() == 0) {
            Page newPage = table.createPage();
            bufferManager.addToBuffer(newPage);
            bufferManager.insertRecord(newPage, record, 0);
            return;
        }

        for (int i = 0; i < table.getNumPages(); i++) {
            Page page = getPage(tableId, i);
            ArrayList<Record> pageRecords = page.getRecords();
            for (int j = 0; j < pageRecords.size(); j++) {
                if (pageRecords.get(j).compareTo(record) > 0) {
                    if (page.canInsertRecord(record)) {
                        bufferManager.insertRecord(page, record, j);
                    } else {
                        // split page
                        Page newPage = page.splitPage();
                        // new page number will now have shifted all those to the right of it over by 1
                        updatePageNumbers(tableId, newPage.getPageId());
                        bufferManager.addToBuffer(newPage);
                    }
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
            idToTable.put(schema.getTableId(), new Table(schema));
        }
    }

    /**
     * Fetch a table
     * @param tableId table id
     * @return Table of tableId
     * @throws NoTableException no table of tableId
     */
    private Table ensureTable(int tableId) throws NoTableException {
        Table table = idToTable.get(tableId);
        if (table == null) {
            throw new NoTableException(tableId);
        }
        return table;
    }

    /**
     * Update page number/ids after a page split
     * @param tableId table id of the page split
     * @param splitPageId id of the new page
     * @throws NoTableException No table of tableId
     */
    private void updatePageNumbers(int tableId, int splitPageId) throws NoTableException {
        Table table = ensureTable(tableId);
        for (int i = splitPageId; i < table.getNumPages(); i++) {
            Page page = bufferManager.getPage(table, i);
            page.incrementPageNumber();
        }

        table.incrementPageCount();
    }
}
