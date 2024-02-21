package catalog;

import java.io.Serializable;

public class AttributeSchema implements Serializable {

    private String attributeName;
    private AttributeType type;
    private boolean isKey;
    private boolean isUnique;
    private boolean isNull;
    private String defaultValue;
    private int attributeId;

    public AttributeSchema(String attributeName, AttributeType type, int attributeId, boolean isKey, boolean isUnique, boolean isNull) {
        this.attributeName = attributeName;
        this.attributeId = attributeId;
        this.type = type;
        this.isKey = isKey;
        this.isUnique = isUnique;
        this.isNull = isNull;
        this.defaultValue = "null";
    }

    public AttributeSchema(String attributeName, AttributeType type, int attributeId, boolean isKey, boolean isUnique, boolean isNull,
            String defaultStr) {
        this.attributeName = attributeName;
        this.attributeId = attributeId;
        this.type = type;
        this.isKey = isKey;
        this.isUnique = isUnique;
        this.isNull = isNull;
        this.defaultValue = defaultStr;
    }

    /**
     * Get the attributes name
     * 
     * @return The attributes name
     */
    public String getAttributeName() {
        return attributeName;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public int setAttributeId(int id) {
        return this.attributeId = id;
    }

    /**
     * Set the attribute name
     * 
     * @param attributeName Name to set
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Get the attributes name
     * 
     * @return The attributes name
     */
    public AttributeType getAttributeType() {
        return type;
    }

    /**
     * Determine if an attribute is a primary key
     * 
     * @return Whether the attrbiute is a primary key
     */
    public boolean isKey() {
        return isKey;
    }

    /**
     * Determine if an attribute is unique
     * 
     * @return Whether the attrbiute is unique
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * Determine if an attribute can be null
     * 
     * @return Whether the attrbiute can be null
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * The default value of the attribute, null if not assigned
     * 
     * @return a String that represents the default value
     */
    public String defaultValue() {
        return this.defaultValue;
    }

    /**
     * Get the size of this attribute in bytes
     * 
     * @return size of this attribute in bytes
     */
    public int getSize() {
        if (this.type.type == AttributeType.TYPE.INT) {
            return Integer.BYTES;
        } else if (this.type.type == AttributeType.TYPE.DOUBLE) {
            return Double.BYTES;
        } else if (this.type.type == AttributeType.TYPE.BOOLEAN) {
            return 1;
        } else if (this.type.type == AttributeType.TYPE.CHAR) {
            return this.type.length;
        } else if (this.type.type == AttributeType.TYPE.VARCHAR) {
            return this.type.length;
        }
        return 0;
    }
}