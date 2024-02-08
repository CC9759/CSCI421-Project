package storageManager;

public class Attribute {
    
    private AttributeType type;
    private Object data;
    private String name;

    public Attribute(AttributeType type, Object data, String name) {
        this.type = type;
        this.data = data;
        this.name = name;
    }

    public AttributeType getAttributeType() {
        return this.type;
    }

    public String getAttributeName() {
        return this.name;
    }

}
