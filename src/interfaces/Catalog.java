import java.util.ArrayList;

public interface Catalog {

    /*
        Instance variables:

        // Singleton Catalog. Can be created from method or read from file.
        // On Database creation, it will be created, the rest of the time it will be read from file
        private Catalog catalogue
        private ArrayList<TableSchema> tables
        private int pageSize
     */

    /**
     * Create a new Catalog instance
     * @param dbLocation location of the database
     * @param pageSize size
     * @return
     */
    public Catalog createCatalog(String dbLocation, int pageSize);

    /**
     * Retrieve the Catalog instance
     * @return Catalog instance
     */
    public Catalog getCatalog();

    /**
     * Read the Catalogue from file
     */
    public void readBinary();

    /**
     * Overwrite the Catalogue to file
     */
    public void writeBinary();

    /**
     * Get a list of the current TableSchemas
     * @return list of all current TableSchemas
     */
    public ArrayList<TableSchema> getTableSchemas();

    /**
     * Get a TableSchema by name
     * @return TableSchema of specific name
     */
    public ArrayList<TableSchema> getTableSchema(String tableSchemaName);

    /**
     * Add a table schema to the catalog
     * @param tableSchema TableSchema to add to the catalog
     */
    public void addTableSchema(TableSchema tableSchema);

    /**
     * Remove a table schema from the catalog
     * @param tableSchemaName Name of table to remove from the catalog
     */
    public void removeTableSchema(String tableSchemaName);

    /**
     * Get the page size of the database
     * @return Page size of the database
     */
    public int getPageSize();
}