package interfaces;
public class AttributeSchema {

    private String attributeName;
    private AttributeType type;
    private boolean isKey;
    private boolean isUnique;
    private boolean isNull;

    public AttributeSchema(String attributeName, AttributeType type, boolean isKey, boolean isUnique, boolean isNull){
        this.attributeName = attributeName;
        this.type = type;
        this.isKey = isKey;
        this.isUnique = isUnique;
        this.isNull = isNull;
    }

    /**
     * Get the attributes name
     * @return The attributes name
     */
    public String getAttributeName(){
        return attributeName;
    }

    /**
     * Set the attribute name
     * @param attributeName Name to set
     */
    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    /**
     * Get the attributes name
     * @return The attributes name
     */
    public AttributeType getAttributeType(){
        return type;
    }

    /**
     * Determine if an attribute is a primary key
     * @return Whether the attrbiute is a primary key
     */
    public boolean isKey(){
        return isKey;
    }

    /**
     * Determine if an attribute is unique
     * @return Whether the attrbiute is unique
     */
    public boolean isUnique(){
        return isUnique;
    }

    /**
     * Determine if an attribute can be null
     * @return Whether the attrbiute can be null
     */
    public boolean isNull(){
        return isNull;
    }

    /**
     * Get the size of this attribute in bytes
     * @return size of this attribute in bytes
     */
    public int getSize() {
        return this.type.length;
    }
}