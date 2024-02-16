package catalog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private String location;
    private int bufferSize;

    private Catalog(String location, int size, int bufferSize) {
        this.location = location;
        this.pageSize = size;
        this.bufferSize = bufferSize;
        // dbLocation is the file location?
    }

    /**
     * Create a new Catalog instance
     * 
     * @param dbLocation location of the database
     * @param pageSize   size
     * @return
     */
    public static synchronized Catalog createCatalog(String dbLocation, int pageSize, int bufferSize) {
        if (catalogue == null)
            catalogue = new Catalog(dbLocation, pageSize, bufferSize);
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
     * Read the Catalogue from file
     * 
     * @param fileLocation the file from where the binary Catalog will be read
     */
    public void readBinary(String fileLocation) {
        // fileLocation Ex. "catalog.txt"
        try (FileInputStream fis = new FileInputStream(fileLocation)) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            Catalog catalog = (Catalog) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Overwrite the Catalogue to file
     *
     * @param fileLocation the file where the binary Catalog will be saved
     */
    public void writeBinary(String fileLocation) {
        // fileLocation Ex. "catalog.txt"
        try (FileOutputStream fos = new FileOutputStream(fileLocation)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(catalogue);
            oos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
            if (tableSchema.getTableName() == tableName) {
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
     * @param tableSchemaId Name of table to remove from the catalog
     */
    public void removeTableSchema(String tableSchemaName) {
        for (int i = 0; i < this.tables.size(); i++) {
            if (this.tables.get(i).getTableName() == tableSchemaName) {
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

    /**
     * 
     */
    public int getTableSchemaLength() {
        return tables.size();
    }

    public String toString(){
        return "Database Location: " + getCatalog().getBufferSize() + 
        "\nPage Size: " +  getCatalog().getPageSize() + 
        "\nBuffer Size: " +  getCatalog().getBufferSize();
    }
}