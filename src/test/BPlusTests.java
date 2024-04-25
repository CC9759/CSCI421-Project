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
import java.util.Collections;
import java.util.List;

public class BPlusTests {
    public static void main(String[] args) throws InvalidTypeException, IllegalOperationException {
        try {
            // Init DB and catalog
            final int PAGE_SIZE = 84;
            final int BUFFER_SIZE = 5;
            Catalog catalog = Catalog.createCatalog("./dbtest/", PAGE_SIZE, BUFFER_SIZE, true);
            AttributeType idType = new AttributeType(AttributeType.TYPE.INT, -1);
            AttributeSchema idSchema = new AttributeSchema("id", idType, 0, true, true, false);
            Attribute id = new Attribute(idSchema, 12);
            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>(Arrays.asList(idSchema));
            TableSchema schema = new TableSchema(0, "users", attributeSchemas);
            catalog.addTableSchema(schema);
            StorageManager.InitStorageManager(BUFFER_SIZE);

            Table table = StorageManager.GetStorageManager().ensureTable(schema.getTableId());

            System.out.println("Treenodes successfully insert");

            int [] inserts = {12, 10, 11, 12, 32, 15, 1, 2, 4, 21, 17, 5, 6, 7, 8, 1, 10, 11, 12, 9, 14, 3, 16, 15, 13, 18, 20, 19, 22,
                    23, 24, 25, 26, 27, 28, 29, 30, 31};
            for (int num : inserts) {
                System.out.println("Inserting " + num);
                Attribute newId = new Attribute(idSchema, num);
                table.insertNode(newId, new Index(num, num));
            }

            var root = table.readNode(0);
            List<TreeNode> leaves = new ArrayList<>();
            root.getAllLeaves(leaves);

            root.printTree();

            ArrayList<Integer> list = new ArrayList<>(Arrays.stream(inserts).boxed().toList());
            Collections.sort(list);

            ArrayList newList = removeDuplicates(list);
            boolean pass = true;

            int listIndex = 0;
            for (int i = 0; i < leaves.size(); i++) {
                var sk = leaves.get(i).getSearchKeys();
                var index = leaves.get(i).getIndices();
                for (int j = 0; j < sk.size(); j++) {
                    pass = pass && (sk.get(j).getData().equals(newList.get(listIndex))) && (newList.get(listIndex).equals(index.get(j).recordPointer));
                    listIndex++;
                }
            }

            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("\nDelete testing\n");
            int[] deleteValues = { 8, 9, 10, 11, 30, 31, 32, 21, };
            for (int num : deleteValues) {
                System.out.println("Deleting " + num);
                table.deleteNode(num);
                newList.remove(Integer.valueOf(num));
                table.readNode(0).printTree();
//                table.readNode(0).printTree();
            }

            leaves.clear();
            root.getAllLeaves(leaves);
            pass = true;
            listIndex = 0;
            for (int i = 0; i < leaves.size(); i++) {
                var sk = leaves.get(i).getSearchKeys();
                var index = leaves.get(i).getIndices();
                for (int j = 0; j < sk.size(); j++) {
                    System.out.println(newList.get(listIndex) + " " + sk.get(j).getData());
                    pass = pass && (sk.get(j).getData().equals(newList.get(listIndex))) && (newList.get(listIndex).equals(index.get(j).recordPointer));
                    listIndex++;
                }
            }

            System.out.println(pass ? "Pass" : "Fail");
            if (!pass) {
                System.exit(1);
            }

            System.out.println("deleting all");
            for (int i = 0; i < inserts.length; i++) {
                System.out.println("Deleting " + inserts[i]);
                table.deleteNode(inserts[i]);
                table.readNode(0).printTree();
            }
            table.readNode(0).printTree();


        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            File testTable = new File("./dbtest/0-index.bin");
            testTable.delete();
        }

    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
}
