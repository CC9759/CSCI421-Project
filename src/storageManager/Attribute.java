package storageManager;

import catalog.AttributeSchema;
import catalog.AttributeType;
import java.io.DataOutputStream;
import java.io.IOException;

public class Attribute extends AttributeSchema implements Comparable<Attribute> {

    private Object data;

    public Attribute(AttributeSchema schema, Object data) {
        super(schema.getAttributeName(), schema.getAttributeType(), schema.isKey(), schema.isUnique(), schema.isNull());
        this.data = data;
    }

    // only useful for varChar and null entries
    public Attribute(AttributeSchema schema, Object data, AttributeType varChar, boolean isNull) {
        super(schema.getAttributeName(), varChar, schema.isKey(), schema.isUnique(), isNull);
        this.data = data;
    }


    public Object getData() {
        return this.data;
    }

    public void serialize(DataOutputStream dos) throws IOException {
        if (data == null) {
            dos.writeByte(0);
            return;
        }
        switch (getAttributeType().type) {
            case INT:
                dos.writeInt((Integer) data);
                break;
            case DOUBLE:
                dos.writeDouble((Double) data);
                break;
            case BOOLEAN:
                dos.writeBoolean((Boolean) data);
                break;
            case CHAR:
            case VARCHAR:
                byte[] stringBytes = ((String) data).getBytes();
                dos.write(stringBytes);
                break;
        }
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
