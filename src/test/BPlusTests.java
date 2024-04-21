package test;

import BPlusTree.*;
import Exceptions.IllegalOperationException;
import Exceptions.InvalidTypeException;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;
import storageManager.Attribute;
import storageManager.Record;
import storageManager.StorageManager;
import storageManager.Table;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BPlusTests {
    public static void main(String[] args) throws InvalidTypeException, IllegalOperationException {
        try {
            // Init DB and catalog
            final int PAGE_SIZE = 300;
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

            TreeNode treeNode = new TreeNode(table, 0, true);
            treeNode.searchKeys.add(id);
            treeNode.indices = new ArrayList<>();
            treeNode.indices.add(new Index(1, 2));
            treeNode.calculateFreeSpace();
            treeNode.writeNode();

            TreeNode readNode = table.readNode(0);
            System.out.println(readNode.searchKeys.get(0).getData());
            System.out.println(readNode.isLeaf);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            File testTable = new File("./dbtest/0-index.bin");
            testTable.delete();
        }

//        BPlusTree tree = new BPlusTree(5);
//        int [] inserts = {12, 10, 11, 12, 32, 15, 1};
//        for (int num : inserts) {
//            Attribute newId = new Attribute(idSchema, num);
//            System.out.println(tree.insert(newId));
//        }

    }
}
