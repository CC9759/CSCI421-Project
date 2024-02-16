import java.util.ArrayList;

import Exceptions.NoTableException;
import Exceptions.PageOverfullException;
import catalog.AttributeSchema;
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

    public void select() {
        // ask storage/buffer manager for table
    }

    // returns flag whether operation is successful
    public Boolean insert(ArrayList<Attribute> recs, String name) {
        // first check that the recs align with table schema
        TableSchema schema = this.cat.getTableSchema(name);

        if (schema == null)
            return false;

        ArrayList<AttributeSchema> refs = schema.getAttributeSchema();

        if (recs.size() != refs.size()) // different amount of attributes
            return false;

        // loop through attribute schema and ensure that insert records are legal
        for (int i = 0; i < recs.size(); i++) {
            Attribute e = recs.get(i);
            AttributeSchema k = refs.get(i);

            if (e.getAttributeType() != k.getAttributeType() || e.getSize() != k.getSize()) 
                return false;
        }

        // the attributes are legal
        Record rec = new Record(recs);
        try {
            this.stor.insertRecord(schema.getTableId(), rec);
        } catch (PageOverfullException | NoTableException e) {
            System.out.println(e);
            return false;
        }

        return true;
    }
}
