package interfaces;

public class AttributeType {
    public enum TYPE {
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

    /**
     * Creates a AttributeType class based on a string.
     * @param typeString the string can be in the format TYPE or TYPE(length).
     */
    public AttributeType(String typeString) {
        String strLength = "";
        if (typeString.contains("(")) {
            strLength = typeString.substring(typeString.indexOf("(") + 1, typeString.indexOf(")"));
            typeString = typeString.substring(0, typeString.indexOf("("));
        }
        if (strLength == "") {
            this.length = -1;
        } else {
            this.length = Integer.parseInt(strLength);
        }
        switch (strLength) {
            case "INT":
                this.type = TYPE.INT;
                break;
            case "DOUBLE":
                this.type = TYPE.DOUBLE;
                break;
            case "BOOLEAN":
                this.type = TYPE.BOOLEAN;
                break;
            case "CHAR":
                this.type = TYPE.CHAR;
                break;
            case "VARCHAR":
                this.type = TYPE.VARCHAR;
                break;
            default:
                // throw exception?
                break;
        }
    }
}