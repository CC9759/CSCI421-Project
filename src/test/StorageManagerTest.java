package test;

import java.util.ArrayList;
import java.util.Arrays;
import catalog.Catalog;
import catalog.TableSchema;
import catalog.AttributeSchema;
import catalog.AttributeType;
import storageManager.*;
import storageManager.Record;

class StorageManagerTest {
    public static void main(String[] args) {
        final int PAGE_SIZE = 150;
        final int BUFFER_SIZE = 5;
        Catalog catalog = Catalog.createCatalog("dbtest/", PAGE_SIZE, BUFFER_SIZE);
        try {
            AttributeType idType = new AttributeType("INT");
            AttributeType nameType = new AttributeType(AttributeType.TYPE.VARCHAR, 32);
            // Define catalog
            AttributeSchema idSchema = new AttributeSchema("id", idType, true, true, false);
            AttributeSchema nameSchema = new AttributeSchema("name", nameType, false, false, true);
            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>(Arrays.asList(idSchema, nameSchema));
            TableSchema schema = new TableSchema(0, "users", attributeSchemas);
            catalog.addTableSchema(schema);

            // Init storage manager
            StorageManager.InitStorageManager(Catalog.getCatalog().getBufferSize());
            StorageManager storageManager = StorageManager.GetStorageManager();

            // Start tests
            System.out.println("Storage Manager should get a record by primary key");

            Attribute id = new Attribute(idSchema, 1);
            Attribute name = new Attribute(nameSchema, null);
            ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(id, name));
            Record testRecord = new Record(attributes);

            storageManager.insertRecord(0, testRecord);
            Record insertRecord = storageManager.getRecordByPrimaryKey(0, id);
            ArrayList<Record> allRecords = storageManager.getAllRecords(0);
            storageManager.flushBuffer();

            Table table = storageManager.getIdToTable().get(0);
            int numPagesOnFile = table.readNumPages();
            System.out.println(numPagesOnFile);
            boolean pass = (insertRecord.compareTo(testRecord) == 0 &&
                            allRecords.size() == 1 &&
                            numPagesOnFile == 1 &&
                            table.getNumPages() == 1);
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("Storage Manager should read a record with a null attribute and successfully delete");
            storageManager.deleteRecord(0, id);
            Record deletedRecord = storageManager.getRecordByPrimaryKey(0, id);

            numPagesOnFile = table.readNumPages();
            pass = deletedRecord == null &&
                    numPagesOnFile == 0 &&
                    table.getNumPages() == 0;

            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("Pages should split on overflow");
            for (int i = 0; i < 5; i++) {
                id = new Attribute(idSchema, i);
                name = new Attribute(nameSchema, "dot" + i);
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }

            Page firstPage = storageManager.getPage(0, 0);
            Page secondPage = storageManager.getPage(0, 1);
            pass =  table.getNumPages() == 2 &&
                    firstPage != null &&
                    secondPage != null &&
                    firstPage.getRecords().size() == 2 &&
                    secondPage.getRecords().size() == 3;
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("StorageManager clears all pages when all records are deleted");
            for (int i = 5; i < 12; i++) {
                id = new Attribute(idSchema, i);
                name = new Attribute(nameSchema, new String("dot" + i));
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }

            for (int i = 0; i < 12; i++) {
                id = new Attribute(idSchema, i);
                storageManager.deleteRecord(0, id);
            }
            pass = storageManager.getPage(0, 0) == null &&
                table.getNumPages() == 0 &&
                table.readNumPages() == 0;
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("Storage manager should be able to mass insert and delete records");
            for (int i = 0; i < 500; i++) {
                id = new Attribute(idSchema, i);
                name = new Attribute(nameSchema, new String("dot" + i));
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }

            for (int i = 0; i < 500; i++) {
                id = new Attribute(idSchema, i);
                Record delete = storageManager.deleteRecord(0, id);
                if (delete == null) {
                    System.out.println("FAILED TO DELETE RECORD " + i);
                    System.exit(1);
                }
            }
            pass = storageManager.getPage(0, 0) == null &&
                    table.getNumPages() == 0 &&
                    table.readNumPages() == 0;
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}