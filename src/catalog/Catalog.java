package catalog;

import java.util.ArrayList;


public class Catalog {

    /*
     * Instance variables:
     * 
     * Singleton Catalog. Can be created from method or read from file.
     * On Database creation, it will be created, the rest of the time it will be
     * read from file
     */
    private static Catalog catalogue = null;
    private ArrayList<TableSchema> tables;
    private int pageSize;

    private Catalog(String location, int size) {
    }

    /**
     * Create a new Catalog instance
     * 
     * @param dbLocation location of the database
     * @param pageSize   size
     * @return
     */
    public static synchronized Catalog createCatalog(String dbLocation, int pageSize) {
        return new Catalog(dbLocation, pageSize);
    }

    /**
     * Retrieve the Catalog instance
     * 
     * @return Catalog instance
     */
    public static Catalog getCatalog() {
        return catalogue;
    }

    /**
     * Read the Catalogue from file
     */
    public void readBinary() {
    }

    /**
     * Overwrite the Catalogue to file
     */
    public void writeBinary() {

    }

    /**
     * Get a list of the current TableSchemas
     * 
     * @return list of all current TableSchemas
     */
    public ArrayList<TableSchema> getTableSchema() {
        return this.tables;
    }

    /**
     * Get a TableSchema by id
     * 
     * @return TableSchema of specific id
     */
    public TableSchema getTableSchema(int Id) {
        for (TableSchema tableSchema : this.tables) {
            if (tableSchema.getTableId() == Id) {
                return tableSchema;
            }
        }
        return null;
    }

    /**
     * Add a table schema to the catalog
     * 
     * @param tableSchema TableSchema to add to the catalog
     */
    public void addTableSchema(TableSchema tableSchema) {
        this.tables.add(tableSchema);
    }

    /**
     * Remove a table schema from the catalog
     * 
     * @param tableSchemaId Id of table to remove from the catalog
     */
    public void removeTableSchema(int tableSchemaId) {
        for (int i = 0; i < this.tables.size(); i++) {
            if (this.tables.get(i).getTableId() == tableSchemaId) {
                this.tables.remove(i);
            }
        }
    }

    /**
     * Get the page size of the database
     * 
     * @return storageManager.Page size of the database
     */
    public int getPageSize() {
        return this.pageSize;
    }
}