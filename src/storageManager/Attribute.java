/**
 * Attribute
 * An attribute containing data in a record
 * @author Daniel Tregea, David Miller
 */
package storageManager;

import catalog.AttributeSchema;
import catalog.AttributeType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Attribute extends AttributeSchema implements Comparable<Attribute>, Cloneable {

    private Object data;

    public Attribute(AttributeSchema schema, Object data) {
        super(schema.getAttributeName(), schema.getAttributeType(), schema.getAttributeId(), schema.isKey(), schema.isUnique(), schema.isNull());
        this.data = data;
    }

    // only useful for varChar and null entries
    public Attribute(AttributeSchema schema, Object data, AttributeType varChar) {
        super(schema.getAttributeName(), varChar, schema.getAttributeId(), schema.isKey(), schema.isUnique(), schema.isNull());
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
    public String toString() {
        return data.toString();
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

    public static int compareTo(Object foo, Object bar) {
        if (foo instanceof Integer ) {
            return Integer.compare((Integer) foo, (Integer) bar);
        } else if(foo instanceof Double) {
            return Double.compare((Double) foo, (Double) bar);
        } else if (foo instanceof Character || foo instanceof String) {
            return ((String) foo).compareTo((String) bar);
        } else if (foo instanceof Attribute) {
            return ((Attribute) foo).compareTo((Attribute) bar);
        } else{
            return ((Boolean) foo).compareTo((Boolean) bar);
        }
    }

    @Override
    public Attribute clone() {
        try {
            Attribute cloned = (Attribute) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
}
