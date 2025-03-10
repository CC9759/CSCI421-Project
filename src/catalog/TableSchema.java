package catalog;

import java.io.Serializable;
import java.util.ArrayList;

public class TableSchema implements Serializable, Cloneable {

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

    public AttributeSchema removeAttributeSchema(String attributeName) {
        // removes a attribute if it belongs to the table. Otherwise, does nothing.
        int removeIndex = -1;
        for (int i = 0; i < this.tableAttributes.size(); i++) {
            if (tableAttributes.get(i).getAttributeName().equals(attributeName)) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex == -1) {
            return null;
        }
        return this.tableAttributes.remove(removeIndex);
    }

    public int getNumPages() {
        return this.numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public void incrementNumPages(int change) {
        this.numPages += change;
    }

    @Override
    public TableSchema clone() throws CloneNotSupportedException {
        TableSchema cloned = (TableSchema) super.clone();
        if (this.tableAttributes != null) {
            cloned.tableAttributes = new ArrayList<>(this.tableAttributes.size());
            for (AttributeSchema attribute : this.tableAttributes) {
                cloned.tableAttributes.add( (AttributeSchema) attribute.clone()); // Ensure deep copy
            }
        }
        return cloned;
    }

    public String getNodeLocation() {
        return Catalog.getCatalog().getLocation() + "/" + getTableId() + "-index" + ".bin";
    }

    public String getPageLocation() {
        return Catalog.getCatalog().getLocation() + "/" + getTableId() + ".bin";
    }

    public AttributeSchema getPrimaryKey() {
        AttributeSchema primaryKey = null;
        for (AttributeSchema attributeSchema : tableAttributes) {
            if (attributeSchema.isKey()) {
                return attributeSchema;
            }
        }
        return null;
    }
}