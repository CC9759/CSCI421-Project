import java.util.ArrayList;

public interface TableSchema {

    /*
     * Instance Variables:
     * private ArrayList<AttributeSchema>;
     * private String tableName;
     * private int tableId;
     */

    /**
     * Get the table id
     * 
     * @return The table id
     */
    public int getTableId();

    /**
     * Get the table name
     * 
     * @return The table name
     */
    public String getTableName();

    /**
     * Get a list of the current AttributeSchema
     * 
     * @return list of all current AttributeSchema
     */
    public ArrayList<AttributeSchema> getAttributeSchema();

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
}