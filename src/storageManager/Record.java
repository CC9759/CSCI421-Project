package storageManager;

import java.util.ArrayList;
import java.util.Map;
import interfaces.AttributeSchema;

/**
 * Record Class, represents a single tuple/entry
 */
public class Record implements Comparable<Record> {
     private Map<String, AttributeSchema> attributes;

     /**
      * gets a specific attribute object based on the given name
      * @param attributeName name of the attribute to grab
      * @return the attribute object
      */
     public AttributeSchema getAttribute(String attributeName){
        return attributes.get(attributeName);
     }

    public AttributeSchema getPrimaryKey(){
         for (AttributeSchema attributeSchema: attributes.values()) {
             if (attributeSchema.isKey()) {
                 return attributeSchema;
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
        for(AttributeSchema attribute : attributes.values()){
            totalSize += attribute.getSize();
        }
        return totalSize;
     }

     /**
      * gets and returns a list of all attribute schemas
      * @return a list of all attribute schemas in the record
      */
     public ArrayList<AttributeSchema> getAttributes(){
         return new ArrayList<>(attributes.values());
     }


     /* 
     * will need to look a schema for record attributes
     * to understand how many to read in and what order
     * !! read in location/size of N attrbiutes listed in the schema !!
     * NOTE: when hit a 00000000 byte, assume null bitmap
     */ 
     public void readRecord(byte[] bytes) {
   
        
         return;      
      }

      // This does not check for whether the records belong to the same table or not
    @Override
    public int compareTo(Record o) {
         interfaces.AttributeType.TYPE type = this.getPrimaryKey().getAttributeType().type; // java
         if (type == interfaces.AttributeType.TYPE.INT) {
             //return Integer.compare(this.getPrimaryKey()) im going to bed
         }
        return 0;
    }
}
