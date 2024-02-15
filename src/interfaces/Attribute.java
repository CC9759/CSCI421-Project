package interfaces;

import catalog.AttributeType;

public interface Attribute {
    
    public AttributeType getAttributeType();
    public boolean isKey();
    public boolean isUnique();
    public boolean isNull();
    public String getAttributeName();

}