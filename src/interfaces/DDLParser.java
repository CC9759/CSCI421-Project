package interfaces;

import java.util.ArrayList;

/**
 * Interface for the DDLParser.DDLParser, will contain/have access
 * to a Catalog and/or Storage Manager instance
 */
public interface DDLParser {
    /**
     * contacts the Catalog and Storage Manager 
     * to add a TableSchema and Table
     * @param tableName the name of the Table to be added
     * @param arguments the list of Table attributes to add
     */
    public void createTable(Catalog catalog, String tableName, ArrayList<String> arguments);
    
    /**
     * contacts the Catalog and Storage Manager
     * to drop/remove a TableSchema and Table
     * @param tableName the name of the Table to drop
     */
    public void dropTable(Catalog catalog, String tableName);

    /**
     * contacts the Catalog and Storage Manager
     * to alter a TableSchema and Table
     * @param tableName the name of the Table to alter
     * @param arguments the list of Table attributes to add/remove
     */
    public void alterTable(Catalog catalog, String tableName, ArrayList<String> arguments);
}
