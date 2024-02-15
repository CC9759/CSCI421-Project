package test;

import java.util.ArrayList;
import java.util.Arrays;

import catalog.Catalog;
import catalog.TableSchema;
import catalog.AttributeSchema;
import catalog.AttributeType;
import storageManager.StorageManager;
import storageManager.Record;
import storageManager.Attribute;

class StorageManagerTest {
    public static void main(String[] args) {
        final int PAGE_SIZE = 512;
        final int BUFFER_SIZE = 5;
        Catalog catalog = Catalog.createCatalog("/db/", PAGE_SIZE, BUFFER_SIZE);
        try {

            // Define catalog
            AttributeSchema idSchema = new AttributeSchema("id", new AttributeType("INT"), true, true, false);
            AttributeSchema nameSchema = new AttributeSchema("name", new AttributeType("VARCHAR"), false, false, false);
            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>(Arrays.asList(idSchema, nameSchema));
            TableSchema schema = new TableSchema(0, "users", attributeSchemas);
            catalog.addTableSchema(schema);

            // Make records
            Attribute id = new Attribute(idSchema, new Integer(1));
            Attribute name = new Attribute(nameSchema, new String("dot"));
            ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(id, name));
            Record testRecord = new Record(attributes);

            // Init storage manager
            StorageManager.InitStorageManager(Catalog.getCatalog().getBufferSize());
            StorageManager storageManager = StorageManager.GetStorageManager();
            

            // Start tests
            storageManager.insertRecord(0, testRecord);
            Record insertRecord = storageManager.getRecordByPrimaryKey(0, id);
            System.out.println("Storage Manager should get a record by primary key: " + (insertRecord.compareTo(testRecord) == 0));

            
            storageManager.deleteRecord(0, id);
            Record deletedRecord = storageManager.getRecordByPrimaryKey(0, id);
            System.out.println("Storage Manager should delete a record: " + (deletedRecord == null));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}