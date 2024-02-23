package storageManager;

import catalog.AttributeSchema;
import catalog.AttributeType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Record Class, represents a single tuple/entry
 */
public class Record implements Comparable<Record> {
     private final HashMap<String, Attribute> attributes;


     public Record(ArrayList<Attribute> attributes) {
        this.attributes = new HashMap<>();
        for (Attribute attribute: attributes) {
            this.attributes.put(attribute.getAttributeName(), attribute);
        }
     }

     /**
      * gets a specific attribute object based on the given name
      * @param attributeName name of the attribute to grab
      * @return the attribute object
      */
     public Attribute getAttribute(String attributeName){
        return attributes.get(attributeName);
     }

    public Attribute setAttribute(String attributeName, Attribute attribute){
        return attributes.put(attributeName, attribute);
    }

    public Attribute removeAttribute(String attributeName){
        return attributes.remove(attributeName);
    }

    public Attribute getPrimaryKey(){
         for (Attribute attribute: attributes.values()) {
             if (attribute.isKey()) {
                 return attribute;
             }
         }
        return null;
    }

     /**
      * grabs the total size of the attribute
      * @return integer representation in bytes of the attribute schema
      */
     public int getSize(){
        int totalSize = 0;
        for(Attribute attribute : attributes.values()){
            totalSize += attribute.getSize();
        }
        return totalSize;
     }

    public int getSizeFile(){
        int totalSize = 4; // num attributes
        int pointerSize = 12; // attr pos, size, id
        for(Attribute attribute : attributes.values()){
            if (attribute.getData() == null) {
                totalSize += pointerSize + 1; // null bitmap plus pointer
            } else if (attribute.getAttributeType().type == AttributeType.TYPE.VARCHAR) {
                totalSize += pointerSize + ((String)attribute.getData()).length();
            } else {
                totalSize += pointerSize + attribute.getSize();
            }
        }
        return totalSize; // doesnt include header side
    }

    public void serialize(DataOutputStream recordDos) throws IOException {
        ArrayList<Attribute> attributes = getAttributes();
        attributes.sort(Comparator.comparing(AttributeSchema::getAttributeName));
        int numAttributes = attributes.size();

        int[] attributePositions = new int[numAttributes];
        int[] attributeSizes = new int[numAttributes];
        int[] attributeIds = new int[numAttributes];
        int recordHeaderSize = 4 + (12 * numAttributes); // num attr then Attr location | size | id for each attr
        int cumulativeAttributeSize = 0;
        ByteArrayOutputStream attributeBaos = new ByteArrayOutputStream();
        DataOutputStream attributeDos = new DataOutputStream(attributeBaos);
        for (int i = 0; i < numAttributes; i++) {
            int startSize = attributeBaos.size();
            attributes.get(i).serialize(attributeDos);
            attributeDos.flush();
            int endSize = attributeBaos.size();
            attributeSizes[i] = endSize - startSize;
            attributePositions[i] = recordHeaderSize + cumulativeAttributeSize;
            attributeIds[i] = attributes.get(i).getAttributeId();
            cumulativeAttributeSize += attributeSizes[i];
        }

        byte[] attributeData = attributeBaos.toByteArray();

        recordDos.writeInt(numAttributes);
        for (int i = 0; i < numAttributes; i++) {
            recordDos.writeInt(attributePositions[i]);
            recordDos.writeInt(attributeSizes[i]);
            recordDos.writeInt(attributeIds[i]);
        }
        recordDos.write(attributeData);
    }

     /**
      * gets and returns a list of all attribute schemas
      * @return a list of all attribute schemas in the record
      */
     public ArrayList<Attribute> getAttributes(){
         return new ArrayList<>(attributes.values());
     }


      // This does not check for whether the records belong to the same table or not
    @Override
    public int compareTo(Record o) {
         return this.getPrimaryKey().compareTo(o.getPrimaryKey());
    }

    @Override
    public String toString() {
        StringBuilder finalStr = new StringBuilder("Record=");
        for (Attribute atr: attributes.values()) {
            finalStr.append("\n\t").append(atr.getAttributeName()).append(": ").append(atr.getData());
        }
        return finalStr.toString();
    }
}
