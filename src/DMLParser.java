import java.util.ArrayList;

import Exceptions.DuplicateKeyException;
import Exceptions.NoTableException;
import Exceptions.PageOverfullException;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;

import storageManager.Attribute;
import storageManager.StorageManager;
import storageManager.Record;

public class DMLParser {
    private StorageManager stor;
    private Catalog cat;

    public DMLParser(StorageManager stor, Catalog cat) {
        this.stor = stor;
        this.cat = cat;
    }

    public void select(String tableName) {
        TableSchema schema = this.cat.getTableSchema(tableName);
        ArrayList<Record> recs;

        if (schema == null) // no table by that name
            return;
        
        try {
            recs = this.stor.getAllRecords(schema.getTableId());
        } catch(NoTableException e) {
            System.out.println(e);
            return;
        }

        // print out attr names
        for (AttributeSchema attr : schema.getAttributeSchema()) {
            System.out.print(attr.getAttributeName() + ", ");
        }

        // print out the tuples
        for (Record rec : recs) {
            System.out.println("");
            ArrayList<Attribute> attrs = rec.getAttributes();
            for (Attribute attr : attrs) {
                System.out.print(attr.getData() + ", ");
            }
        }
    }

    public void displaySchema() {
        // displays the schema of the catalog, just prints out catalog's toString
        System.out.println(this.cat.toString());
    }

    public void displayInfo (String tableName) {
        TableSchema schema = this.cat.getTableSchema(tableName);

        if (schema == null) { // no table by that name
            System.out.println(tableName + " DNE");
            return;
        }
        
        System.out.println("Table: " + schema.getTableName());

        System.out.print("Attributes: ");
        for (AttributeSchema attr : schema.getAttributeSchema()) {
            System.out.print(attr.getAttributeName() + ", ");
        }

        System.out.println("Number of pages: "+ schema.getNumPages());

        ArrayList<Record> recs;
        try {
            recs = this.stor.getAllRecords(schema.getTableId());
        } catch(NoTableException e) {
            System.out.println(e);
            return;
        }

        System.out.println("Number of Records: " + recs.size());

    }

    // for whoever is making the recs array
    // possible implementation is to use 3 default schemas, they will get overwritten either way
    // returns flag whether operation is successful
    //
    // NOTE
    // not sure how varchar type is handled when written
    // the attribute type needs to handle N length give M max size
    // NOTE
    public Boolean insert(ArrayList<Attribute> recs, String name) {
        // first check that the recs align with table schema
        TableSchema schema = this.cat.getTableSchema(name);

        if (schema == null) { // no table by that name
            System.out.println("No table with name: " + name);
            return false;
        }

        ArrayList<AttributeSchema> refs = schema.getAttributeSchema();

        if (recs.size() != refs.size()) { // different amount of attributes
            System.out.println("Unexpected amount of records.");
            return false;
        }

        ArrayList<Attribute> legal_recs = new ArrayList<Attribute>();
        // loop through attribute schema and ensure that inserted records are legal
        for (int i = 0; i < recs.size(); i++) {
            Attribute e = recs.get(i);
            AttributeSchema k = refs.get(i);

            // if value of data is null
            if (e.isNull()) {
                if (k.isUnique() || k.isKey() || k.isNull()) { // value cannot be null
                    System.out.println("The #" + i + " cannot be null." );
                    return false;
                }
                // value can be null
                legal_recs.add(new Attribute(k, null, k.getAttributeType()));
                continue;
            }

            // assume all strings come in as CHAR
            if (k.getAttributeType().type == AttributeType.TYPE.CHAR || k.getAttributeType().type == AttributeType.TYPE.VARCHAR) {
                if (e.getAttributeType().type != AttributeType.TYPE.CHAR) {// expecting a char
                    System.out.println("The #" + i + " should be a char/varchar type." );
                    return false;
                }

                if (e.getSize() > k.getSize()) { // char is too big
                    System.out.println("The #" + i + " is a char/varchar type but is too large." );
                    return false;
                }

                // expected char of length N but got char length M
                if (k.getAttributeType().type == AttributeType.TYPE.CHAR && k.getSize() != ((String) e.getData()).length()) {
                    System.out.println("The #" + i + " is a char of required length " + k.getSize() + "." );
                    return false;
                }

                // replace with correct schema and data input
                if (k.getAttributeType().type != AttributeType.TYPE.VARCHAR) {
                    // if varChar, then create new attribute type with exact attr size
                    AttributeType varchar = new AttributeType(catalog.AttributeType.TYPE.VARCHAR, ((String)e.getData()).length());
                    legal_recs.add(new Attribute(k, (String) e.getData(), varchar));
                } else {
                    legal_recs.add(new Attribute(k, (String) e.getData()));
                }

            } else if (k.getAttributeType().type == AttributeType.TYPE.INT || k.getAttributeType().type == AttributeType.TYPE.DOUBLE) {
                // replace with correct schema and data input
                if (e.getAttributeType().type != AttributeType.TYPE.INT)
                    legal_recs.add(new Attribute(k, (int) e.getData()));
                else if (e.getAttributeType().type != AttributeType.TYPE.DOUBLE) 
                    legal_recs.add(new Attribute(k, (double) e.getData()));
                else {
                    System.out.println("The #" + i + " should be a int/double type.");
                    return false;
                }

            } else if (k.getAttributeType().type == AttributeType.TYPE.BOOLEAN) {
                if (e.getAttributeType().type != AttributeType.TYPE.BOOLEAN) { // expecting a bool
                    System.out.println("The #" + i + " should be a bool type.");
                    return false;
                }

                // replace with correct schema and data input
                legal_recs.add(new Attribute(k, (boolean) e.getData()));
            }
        }

        // all the attributes are legal, and have a correct schema
        Record record = new Record(legal_recs);
        try {
            this.stor.insertRecord(schema.getTableId(), record);
        } catch (PageOverfullException | NoTableException | DuplicateKeyException error) {
            System.out.println(error);
            return false;
        }

        return true;
    }
}
