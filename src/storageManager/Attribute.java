package storageManager;

import interfaces.AttributeSchema;

public class Attribute extends AttributeSchema implements Comparable<Attribute> {

    private Object data;

    public Attribute(String attributeName, interfaces.AttributeType type, boolean isKey, boolean isUnique, boolean isNull, Object data) {
        super(attributeName, type, isKey, isUnique, isNull);
        this.data = data;
    }


    public Object getData() {
        return this.data;
    }

    @Override
    public int compareTo(Attribute o) {
        if (this.data instanceof Integer || this.data instanceof Double) {
            return Double.compare((Double) this.data, (Double) o.data);
        } else if (this.data instanceof Character || this.data instanceof String) {
            return ((String) this.data).compareTo((String) o.data);
        } else {
            return ((Boolean) this.data).compareTo((Boolean) o.data);
        }
    }
}
