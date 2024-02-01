public interface AttributeSchema {

    /*
        Instance Variables:
        private String attributeName;
        private AttributeType type;
        private boolean isKey;
        private boolean isUnique;
        private boolean isNull;
     */

    /**
     * Get the attributes name
     * @return The attributes name
     */
    public String getAttributeName();

    /**
     * Set the attribute name
     * @param attributeName Name to set
     */
    public void setAttributeName(String attributeName);

    /**
     * Get the attributes name
     * @return The attributes name
     */
    public AttributeType getAttributeType();

    /**
     * Determine if an attribute is a primary key
     * @return Whether the attrbiute is a primary key
     */
    public boolean isKey();

    /**
     * Determine if an attribute is unique
     * @return Whether the attrbiute is unique
     */
    public boolean isUnique();

    /**
     * Determine if an attribute can be null
     * @return Whether the attrbiute can be null
     */
    public boolean isNull();
}