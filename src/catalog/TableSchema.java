package catalog;

import java.io.Serializable;
import java.util.ArrayList;

public class TableSchema implements Serializable {

    /*
     * Instance Variables:
     */
    private ArrayList<AttributeSchema> tableAttributes;
    private String tableName;
    private int tableId;

    private int numPages;

    public TableSchema(int id, String name, ArrayList<AttributeSchema> attributes) {
        this.tableId = id;
        this.tableName = name;
        this.tableAttributes = attributes;
        this.numPages = 0;
    }

    public int getTableId() {
        return tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<AttributeSchema> getAttributeSchema() {
        return tableAttributes;
    }

    public AttributeSchema getAttributeSchema(String attributeName) {
        for (int i = 0; i < this.tableAttributes.size(); i++) {
            if (tableAttributes.get(i).getAttributeName().equals(attributeName))
                return tableAttributes.get(i);

        }
        return null;
    }

    public AttributeSchema getAttributeSchema(int attributeId) {
        for (int i = 0; i < this.tableAttributes.size(); i++) {
            if (tableAttributes.get(i).getAttributeId() == attributeId)
                return tableAttributes.get(i);

        }
        return null;
    }

    public void addAttributeSchema(AttributeSchema attributeSchema) {
        int maxId = 0;
        for (AttributeSchema as: this.tableAttributes) {
            maxId = Math.max(maxId, as.getAttributeId());
        }
        attributeSchema.setAttributeId(maxId + 1);
        this.tableAttributes.add(attributeSchema);
    }

    public void removeAttributeSchema(String attributeName) {
        // removes a attribute if it belongs to the table. Otherwise, does nothing.
        int removeIndex = -1;
        for (int i = 0; i < this.tableAttributes.size(); i++) {
            if (tableAttributes.get(i).getAttributeName() == attributeName) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex == -1) {
            return;
        }
        this.tableAttributes.remove(removeIndex);
    }

    public int getNumPages() {
        return this.numPages;
    }

    public void incrementNumPages() {
        this.numPages++;
    }
}