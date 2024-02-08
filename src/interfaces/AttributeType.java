package interfaces;

class AttributeType {
    private enum TYPE {
        INT, DOUBLE, BOOLEAN, CHAR, VARCHAR
    }

    public int length; // for char and varchar types. Null on other types
    public TYPE type;

    public AttributeType(TYPE type) {
        this.type = type;
        this.length = -1;
    }

    public AttributeType(TYPE type, int length) {
        this.type = type;
        this.length = length;
    }
}