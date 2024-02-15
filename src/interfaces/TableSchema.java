package interfaces;

import java.util.ArrayList;

import catalog.AttributeSchema;

public interface TableSchema {

    /*
     * Instance Variables:
     * private ArrayList<AttributeSchema>;
     * private String tableName;
     * private int tableId;
     */

    /**
     * Get the tables id
     * 
     * @return The tables name
     */
    public int getTableId();

    /**
     * Get the tables name
     * 
     * @return The tables name
     */
    public String getTableName();

    /**
     * Get a list of the current AttributeSchemas
     * 
     * @return list of all current AttributeSchemas
     */
    public ArrayList<AttributeSchema> getAttributeSchemas();

    /**
     * Get an AttributeSchema by name
     * 
     * @return AttributeSchema of specific name
     */
    public AttributeSchema getAttributeSchema(String attributeName);

    /**
     * Add a attribute schema to the catalog
     * 
     * @param attributeSchema AttributeSchema to add to the table
     */
    public void addAttributeSchema(AttributeSchema attributeSchema);

    /**
     * Remove a table schema from the catalog
     * 
     * @param attributeName Name of the attribute to remove
     */
    public void removeAttributeSchema(String attributeName);

    public int getNumPages();

    public void incrementNumPages();
}