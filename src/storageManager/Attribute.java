package storageManager;

import catalog.AttributeSchema;

public class Attribute extends AttributeSchema implements Comparable<Attribute> {

    private Object data;

    public Attribute(AttributeSchema schema, Object data) {
        super(schema.getAttributeName(), schema.getAttributeType(), schema.isKey(), schema.isUnique(), schema.isNull());
        this.data = data;
    }


    public Object getData() {
        return this.data;
    }

    @Override
    public int compareTo(Attribute o) {
        if (this.data instanceof Integer ) {
            return Integer.compare((Integer) this.data, (Integer) o.data);
        } else if(this.data instanceof Double) {
            return Double.compare((Double) this.data, (Double) o.data);
        } else if (this.data instanceof Character || this.data instanceof String) {
            return ((String) this.data).compareTo((String) o.data);
        } else {
            return ((Boolean) this.data).compareTo((Boolean) o.data);
        }
    }
}
