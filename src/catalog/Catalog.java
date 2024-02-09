package catalog;

import java.util.ArrayList;

import interfaces.TableSchema;


public class Catalog {

    /*
     * Instance variables:
     * 
     * Singleton Catalog. Can be created from method or read from file.
     * On Database creation, it will be created, the rest of the time it will be
     * read from file
     */
    private Catalog catalogue;
    private ArrayList<TableSchema> tables;
    private int pageSize;

    /**
     * Create a new Catalog instance
     * 
     * @param dbLocation location of the database
     * @param pageSize   size
     * @return
     */
    public Catalog(String dbLocation, int pageSize) {

    }

    /**
     * Retrieve the Catalog instance
     * 
     * @return Catalog instance
     */
    public Catalog getCatalog() {
        return null;
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
        return null;
    }

    /**
     * Get a TableSchema by id
     * 
     * @return TableSchema of specific id
     */
    public ArrayList<TableSchema> getTableSchema(int Id) {
        return null;
    }

    /**
     * Add a table schema to the catalog
     * 
     * @param tableSchema TableSchema to add to the catalog
     */
    public void addTableSchema(TableSchema tableSchema) {

    }

    /**
     * Remove a table schema from the catalog
     * 
     * @param tableSchemaName Id of table to remove from the catalog
     */
    public void removeTableSchema(String tableSchemaId) {

    }

    /**
     * Get the page size of the database
     * 
     * @return storageManager.Page size of the database
     */
    public int getPageSize() {
        return 0;
    }
}