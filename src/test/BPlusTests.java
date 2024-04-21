package test;

import BPlusTree.*;
import Exceptions.IllegalOperationException;
import Exceptions.InvalidTypeException;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;
import storageManager.Attribute;
import storageManager.StorageManager;
import storageManager.Table;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class BPlusTests {
    public static void main(String[] args) throws InvalidTypeException, IllegalOperationException {
        try {
            // Init DB and catalog
            final int PAGE_SIZE = 155;
            final int BUFFER_SIZE = 5;
            Catalog catalog = Catalog.createCatalog("./dbtest/", PAGE_SIZE, BUFFER_SIZE);
            AttributeType idType = new AttributeType(AttributeType.TYPE.VARCHAR, 16);
            AttributeSchema idSchema = new AttributeSchema("id", idType, 0, true, true, false);
            Attribute id = new Attribute(idSchema, "Daniel");
            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>(Arrays.asList(idSchema));
            TableSchema schema = new TableSchema(0, "users", attributeSchemas);
            catalog.addTableSchema(schema);
            StorageManager.InitStorageManager(BUFFER_SIZE);

            Table table = StorageManager.GetStorageManager().ensureTable(schema.getTableId());

            boolean pass;
            System.out.println("Individual Nodes are written and read to memory");
            TreeNode treeNode = new TreeNode(table, 0, true);
            treeNode.addKey(id);
            treeNode.calculateFreeSpace();
            treeNode.writeNode();

            TreeNode root = table.readNode(0);
            pass = root != null;
            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("Treenodes successfully insert");

            String [] inserts = {"12", "13", "14", "15", "16"};
            for (String num : inserts) {
                Attribute newId = new Attribute(idSchema, num);
                root.insert(newId);
                root = table.readNode(0);

            }

            System.out.println("hi");



        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            File testTable = new File("./dbtest/0-index.bin");
            testTable.delete();
        }

//

    }
}
