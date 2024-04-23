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
            final int PAGE_SIZE = 84;
            final int BUFFER_SIZE = 5;
            Catalog catalog = Catalog.createCatalog("./dbtest/", PAGE_SIZE, BUFFER_SIZE);
            AttributeType idType = new AttributeType(AttributeType.TYPE.INT, -1);
            AttributeSchema idSchema = new AttributeSchema("id", idType, 0, true, true, false);
            Attribute id = new Attribute(idSchema, 12);
            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>(Arrays.asList(idSchema));
            TableSchema schema = new TableSchema(0, "users", attributeSchemas);
            catalog.addTableSchema(schema);
            StorageManager.InitStorageManager(BUFFER_SIZE);

            Table table = StorageManager.GetStorageManager().ensureTable(schema.getTableId());

            boolean pass;
            System.out.println("Individual Nodes are written and read to memory");
//            TreeNode treeNode = new TreeNode(table, 0, true);
//            treeNode.insert(id, new Index(12, 12));
//
//            TreeNode root = table.readNode(0);
//            pass = root != null;
//            System.out.println(pass ? "Pass" : "Fail");
//            if (!pass) {
//                System.exit(1);
//            }

            TreeNode root = table.readNode(0);

            System.out.println("Treenodes successfully insert");

            int [] inserts = {12, 10, 11, 12, 32, 15, 1};
            for (int num : inserts) {
                System.out.println("Inserting " + num);
                Attribute newId = new Attribute(idSchema, num);
                root.insert(newId, new Index(num, num));
                root = table.readNode(0);

            }
            root.printTree();
            System.out.println("-------------------");


            inserts = new int[]{2, 4, 21, 17, 5, 6, 7, 8, 1, 10, 11, 12, 9, 14, 3, 16, 15, 13, 18, 20, 19, 22,
                    23, 24, 25, 26, 27, 28, 29, 30, 31};
            for (int num : inserts) {
                System.out.println("Inserting " + num);
                Attribute newId = new Attribute(idSchema, num);
                root.insert(newId, new Index(num, num));
                root = table.readNode(0);
                root.printTree();

                System.out.println("-------------------");
            }

            System.out.println("\nDelete testing\n");
            int[] deleteValues = { 8, 9, 10, 11, 30, 31, 32, 21, };
            for (int num : deleteValues) {
                System.out.println("Deleting " + num);
//                Attribute toDelete = new Attribute(idSchema, num);
                root.delete(num);
                root = table.readNode(0);
                root.printTree();
                System.out.println("-------------------");

            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            File testTable = new File("./dbtest/0-index.bin");
            testTable.delete();
        }

    }
}
