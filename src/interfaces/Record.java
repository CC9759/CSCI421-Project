import java.util.ArrayList;

public interface Record {
    
    /*
     Instance variables
     private Map<String, Attribute> attributes
     */

     public Attribute getAttribute(String attributeName);

     public int getSize();
     public ArrayList<Attribute> getAttributes();
}
