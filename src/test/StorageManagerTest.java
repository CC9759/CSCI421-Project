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
import storageManager.Page;

class StorageManagerTest {
    public static void main(String[] args) {
        final int PAGE_SIZE = 160;
        final int BUFFER_SIZE = 5;
        Catalog catalog = Catalog.createCatalog("/dbtest/", PAGE_SIZE, BUFFER_SIZE);
        try {
            AttributeType idType = new AttributeType("INT");
            AttributeType nameType = new AttributeType(AttributeType.TYPE.VARCHAR, 32);
            // Define catalog
            AttributeSchema idSchema = new AttributeSchema("id", idType, true, true, false);
            AttributeSchema nameSchema = new AttributeSchema("name", nameType, false, false, false);
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
            System.out.println("Storage Manager should get a record by primary key");
            storageManager.insertRecord(0, testRecord);
            Record insertRecord = storageManager.getRecordByPrimaryKey(0, id);
            ArrayList<Record> allRecords = storageManager.getAllRecords(0);
            boolean pass = (insertRecord.compareTo(testRecord) == 0 &&
                            allRecords.size() == 1);
            System.out.println(pass ? "Pass" : "Fail");


            System.out.println("Storage Manager should delete a record");
            storageManager.deleteRecord(0, id);
            Record deletedRecord = storageManager.getRecordByPrimaryKey(0, id);
            pass = deletedRecord == null;
            System.out.println(pass ? "Pass" : "Fail");


            System.out.println("Pages should split on overflow");
            for (int i = 0; i < 5; i++) {
                id = new Attribute(idSchema, Integer.valueOf(i));
                name = new Attribute(nameSchema, new String("dot" + i));
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }
            Page firstPage = storageManager.getPage(0, 0);
            Page secondPage = storageManager.getPage(0, 1);

            pass =  firstPage != null &&
                    secondPage != null &&
                    firstPage.getRecords().size() == 2 &&
                    secondPage.getRecords().size() == 3;
            System.out.println(pass ? "Pass" : "Fail");

            System.out.println("StorageManager clears all pages when all records are deleted");
            for (int i = 5; i < 12; i++) {
                id = new Attribute(idSchema, Integer.valueOf(i));
                name = new Attribute(nameSchema, new String("dot" + i));
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }
            
            for (int i = 0; i < 12; i++) {
                id = new Attribute(idSchema, Integer.valueOf(i));
                name = new Attribute(nameSchema, new String("dot" + i));
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.deleteRecord(0, id);
            }
            pass = storageManager.getPage(0, 0).getRecords().size() == 0;
            System.out.println(pass ? "Pass" : "Fail");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}