package interfaces;

import java.util.ArrayList;

/**
 * Interface for the DMLParser, will contain/have access 
 * to a Storage Manager instance
 */
public interface DMLParser {

    /**
     * method for the insert functionality,inserts data into a 
     * given table with a list of values. I'm not sure how we'll handle
     * values rn, so it'll be a string for now.
     * 
     * Command format: insert into <name> values <tuples>;
     * 
     * @param table the name of the table to insert data into
     * @param values the list of values to insert into the table
     */
    public void insert(String table, ArrayList<String> values);

    /**
     * displays the schema in the catalog, I assume it just calls the toString
     * in Catalog
     * 
     * Command format: display schema;
     */
    public void displaySchema();

    /**
     * displays the information of a table via the Storage Manager
     * 
     * Command format: display info <name>;
     * 
     * @param table the name of the table to display info about
     */
    public void displayInfo(String table);

    /**
     * accesses the entire data in a given table and displays in
     * readable format
     * 
     * Command format: select * from <name>;
     * 
     * @param table the name of the table to select
     */
    public void select(String table);
}
