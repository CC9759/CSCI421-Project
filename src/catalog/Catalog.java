package catalog;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class Catalog implements Serializable {

    private static Catalog catalogue = null;
    private ArrayList<TableSchema> tables;
    private int pageSize;
    private String location;
    private int bufferSize;
    private boolean indexing;

    private Catalog(String location, int size, int bufferSize, boolean indexing) {
        this.location = location;
        this.pageSize = size;
        this.bufferSize = bufferSize;
        this.tables = new ArrayList<TableSchema>();
        this.indexing = indexing;
        // dbLocation is the file location?
    }

    /**
     * Create a new Catalog instance
     * 
     * @param dbLocation location of the database
     * @param pageSize   size
     * @return
     */
    public static synchronized Catalog createCatalog(String dbLocation, int pageSize, int bufferSize, boolean indexing) {
        if (catalogue == null)
            catalogue = new Catalog(dbLocation, pageSize, bufferSize, indexing);
        return catalogue;
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
     * getter for indexing
     * 
     * @return boolean flag of indexing
     */
    public boolean getIndexing() {
        return this.indexing;
    }

    /**
     * setter for indexing
     * 
     * @param indexing the new val for indexing
     */
    public void setIndexing(boolean indexing) {
        this.indexing = indexing;
    }

    /**
     * Read the Catalogue from file
     * 
     * @param fileLocation the file from where the binary Catalog will be read
     */
    public static void readBinary(String fileLocation) {
        // fileLocation Ex. "catalog.txt"
        try (FileInputStream fis = new FileInputStream(fileLocation)) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            catalogue = (Catalog) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overwrite the Catalogue to file
     */
    public void writeBinary() {
        // fileLocation Ex. "catalog.txt"
        try (FileOutputStream fos = new FileOutputStream(this.location + "/catalog.bin")) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(catalogue);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Get a TableSchema by id
     * 
     * @return TableSchema of specific id
     */
    public TableSchema getTableSchema(String tableName) {
        for (TableSchema tableSchema : this.tables) {
            if (Objects.equals(tableSchema.getTableName(), tableName)) {
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
     * Remove a table schema from the catalog
     * 
     * @param tableSchemaName Name of table to remove from the catalog
     */
    public void removeTableSchema(String tableSchemaName) {
        for (int i = 0; i < this.tables.size(); i++) {
            if (Objects.equals(this.tables.get(i).getTableName(), tableSchemaName)) {
                this.tables.remove(i);
            }
        }
    }

    /**
     * Get the page size of the database
     * 
     * @return storageManager.Page size of the database
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Get the page size of the database
     * 
     * @return storageManager.Page size of the database
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Get the page size of the database
     * 
     * @return storageManager.Page size of the database
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * 
     */
    public int getTableSchemaLength() {
        return tables.size();
    }

    public String toString() {
        return "Database Location: " + getCatalog().getLocation() +
                "\nPage Size: " + getCatalog().getPageSize() +
                "\nBuffer Size: " + getCatalog().getBufferSize();
    }
}