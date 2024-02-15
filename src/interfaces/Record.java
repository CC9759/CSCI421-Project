package interfaces;

import java.util.ArrayList;

import catalog.AttributeSchema;

public interface Record {

    /*
     Instance variables
     private Map<String, Attribute> attributes
     */

     public AttributeSchema getAttribute(String attributeName);

     public int getSize();
     
     public ArrayList<AttributeSchema> getAttributes();
}