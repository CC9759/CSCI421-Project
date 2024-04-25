/**
 * Storage Manager test
 * Unit tests for storage manager
 * @author Daniel Tregea
 */
package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import DDLParser.DDLParser;
import catalog.Catalog;
import catalog.TableSchema;
import catalog.AttributeSchema;
import catalog.AttributeType;
import storageManager.*;
import storageManager.Record;

class StorageManagerTest {
    public static void main(String[] args) {
        final int PAGE_SIZE = 300;
        final int BUFFER_SIZE = 5;
        Catalog catalog = Catalog.createCatalog("./dbtest/", PAGE_SIZE, BUFFER_SIZE, true);
        try {
            AttributeType idType = new AttributeType("integer");
            AttributeType nameType = new AttributeType(AttributeType.TYPE.VARCHAR, 32);
            // Define catalog
            AttributeSchema idSchema = new AttributeSchema("id", idType, 0, true, true, false);
            AttributeSchema nameSchema = new AttributeSchema("name", nameType, 1, false, false, true);
            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>(Arrays.asList(idSchema, nameSchema));
            TableSchema schema = new TableSchema(0, "users", attributeSchemas);
            catalog.addTableSchema(schema);

            // Init storage manager
            StorageManager.InitStorageManager(Catalog.getCatalog().getBufferSize());
            StorageManager storageManager = StorageManager.GetStorageManager();

            // Start tests
            System.out.println("Storage Manager should get a record by primary key");

            Attribute id = new Attribute(idSchema, 1);
            Attribute name = new Attribute(nameSchema, "dot");
            ArrayList<Attribute> attributes = new ArrayList<>(Arrays.asList(id, name));
            Record testRecord = new Record(attributes);

            storageManager.insertRecord(0, testRecord);
            Record insertRecord = storageManager.getRecordByPrimaryKey(0, id);
            ArrayList<Record> allRecords = storageManager.getAllRecords(0);
            storageManager.flushBuffer();

            Table table = storageManager.getIdToTable().get(0);
            int numPagesOnFile = table.readNumPages();
            boolean pass = (insertRecord.compareTo(testRecord) == 0 &&
                            allRecords.size() == 1 &&
                            numPagesOnFile == 1 &&
                            table.getNumPages() == 1 &&
                            insertRecord.getAttributes().get(0).getData().equals("dot"));
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
                System.out.println("insert " + i);
                id = new Attribute(idSchema, i);
                name = new Attribute(nameSchema, "dot" + i);
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }
            for (int i = 0; i < 500; i++) {
                System.out.println("delete " + i);

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

            System.out.println("Storage manager should be able to update a record");
            id = new Attribute(idSchema, 500);
            name = new Attribute(nameSchema, "dot");
            attributes = new ArrayList<>(Arrays.asList(id, name));
            testRecord = new Record(attributes);
            storageManager.insertRecord(0, testRecord);

            // update record
            name = new Attribute(nameSchema, "pinky");
            testRecord.setAttribute("name", name);
            storageManager.updateRecord(table.schema.getTableId(), testRecord);
            Record updatedRecord = storageManager.getRecordByPrimaryKey(table.schema.getTableId(), id);

            pass = ((String)updatedRecord.getAttribute("name").getData()).equals("pinky");
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("Storage manager should update records on alter table ADD");
            DDLParser ddlParser = new DDLParser();
            for (int i = 0; i < 500; i++) {
                id = new Attribute(idSchema, i);
                name = new Attribute(nameSchema, "dot" + i);
                attributes = new ArrayList<>(Arrays.asList(id, name));
                testRecord = new Record(attributes);
                storageManager.insertRecord(0, testRecord);
            }
            ddlParser.alterTable(Catalog.getCatalog(), "users", "add email varchar(32) default pinky@gmail.com");
            allRecords = storageManager.getAllRecords(0);
            pass = true;
            for (Record record: allRecords) {
                pass = pass && record.getAttribute("email").getData().equals("pinky@gmail.com");

            }
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("Storage manager should update records on alter table DROP");
            ddlParser.alterTable(Catalog.getCatalog(), "users", "drop email");
            allRecords = storageManager.getAllRecords(0);
            pass = true;
            for (Record record: allRecords) {
                pass = pass && record.getAttribute("email") == null;

            }
            pass = updatedRecord.getAttribute("email") == null &&
                    catalog.getTableSchema("users").getAttributeSchema().size() == 2;
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            File testTable = new File("./dbtest/0.bin");
            testTable.delete();
            File testIndex = new File("./dbtest/0-index.bin");
            testIndex.delete();
        }

    }
}