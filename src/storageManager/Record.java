package storageManager;

import java.util.ArrayList;
import java.util.Map;
import interfaces.AttributeSchema;


/**
 * Record Class, represents a single tuple/entry
 */
public class Record {
     private Map<String, AttributeSchema> attributes;

     /**
      * gets a specific attribute object based on the given name
      * @param attributeName name of the attribute to grab
      * @return the attribute object
      */
     public AttributeSchema getAttribute(String attributeName){
        return attributes.get(attributeName);
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
}
