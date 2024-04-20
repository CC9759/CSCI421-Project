package test;

import storageManager.Attribute;
import BPlusTree.BPlusTree;
import BPlusTree.TreeNode;
import Exceptions.InvalidTypeException;
import catalog.AttributeSchema;
import catalog.AttributeType;

public class BPlusTreeTests {

    private static void testInsert(BPlusTree tree, Attribute value) {
        boolean result = tree.insert(value);
        if (result) {
            System.out.println("Insertion successful for value: " + value.getData());
        } else {
            System.out.println("Insertion failed for value: " + value.getData());
        }
    }

    private static void testFind(BPlusTree tree, Object value) {
        TreeNode node = tree.find(value);
        if (node != null && node.searchKeys.stream().anyMatch(k -> k.getData().equals(value))) {
            System.out.println("Value found: " + value);
        } else {
            System.out.println("Value not found: " + value);
        }
    }

    private static void testDelete(BPlusTree tree, Object value) {
        boolean result = tree.delete(value);
        if (result) {
            System.out.println("Deletion successful for value: " + value);
        } else {
            System.out.println("Deletion failed for value: " + value);
        }
    }

    public static void main(String[] args) throws InvalidTypeException {
        BPlusTree tree = new BPlusTree(5); 
        AttributeSchema idSchema = new AttributeSchema("id", new AttributeType("integer"), 0, true, true, false);

        // Test Insertions
        System.out.println("\n--- Insertion Tests ---");
        testInsert(tree, new Attribute(idSchema, 10));
        testInsert(tree, new Attribute(idSchema, 20));
        testInsert(tree, new Attribute(idSchema, 30));
        testInsert(tree, new Attribute(idSchema, 5));
        testInsert(tree, new Attribute(idSchema, 35));
        testInsert(tree, new Attribute(idSchema, 5)); //dupe

        // Test Finding
        System.out.println("\n--- Find Tests ---");
        testFind(tree, 10);
        testFind(tree, 20);
        testFind(tree, 30);
        testFind(tree, 5);
        testFind(tree, 99); //non-existent

        // Test Deletions
        System.out.println("\n--- Deletion Tests ---");
        testDelete(tree, 20);
        testDelete(tree, 5); 
        testDelete(tree, 99); 

        // Test Tree Structure after operations
        System.out.println("\n--- Tree Structure after operations ---");
        tree.printTree();

        // Test Complex Operations: Splits and Merges
        System.out.println("\n--- Complex Operations (Splits and Merges) ---");
        for (int i = 40; i <= 80; i += 5) {
            testInsert(tree, new Attribute(idSchema, i));
        }
        tree.printTree();

        for (int i = 40; i <= 80; i += 10) { 
            testDelete(tree, i);
        }
        tree.printTree();
    }
}
