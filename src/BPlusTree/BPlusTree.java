package BPlusTree;

import Exceptions.IllegalOperationException;
import Exceptions.InvalidTypeException;
import catalog.AttributeSchema;
import catalog.Catalog;
import storageManager.Attribute;
import storageManager.Table;

import java.io.File;

public class BPlusTree {
    public int N; // Size of the b+ tree to get size of nodes and separation
    private TreeNode root;

    private int numNodes;
    private Table table;


    public BPlusTree(Table table) {
        AttributeSchema primaryKey = table.schema.getPrimaryKey();
        // - 5 for numNodes and isLeaf
        // 4 + 4 + 1 for node info
        this.N = ((Catalog.getCatalog().getPageSize() - 5) / (8 + primaryKey.getSize()));
        this.table = table;
        this.table.root = this;
        this.numNodes = readNumNodes();
        if (this.numNodes > 0) {
            try {
                this.root = table.readNode(0);
            } catch (IllegalOperationException e) {
                this.root = new TreeNode(table, 0, true);
            }
        } else {
            this.root = new TreeNode(table, 0, true);
        }
        this.root.isLeaf = true;


    }

    /**
     * function to insert a value in the B+ Tree
     *
     * @param value the specified value that will be insert
     * @return a boolean. true if the value was succesfully added to the tree.
     * otherwise, it returns false
     */
    public boolean insert(Attribute value) throws IllegalOperationException {
        return root.insert(value);
    }

    public boolean delete(Object value) throws IllegalOperationException {
        return root.delete(value);
    }

    public TreeNode find(Object value) throws IllegalOperationException {
        return root.find(value, root.nodeNumber);
    }

    public int getTreeSize() {
        return N;
    }

    public int readNumNodes() {
        try {
            String location = table.schema.getPageLocation();
            File file = new File(location);

            if (!file.exists()) {
                return 0;
            }
            long fileSize = file.length();
            int pageSize = Catalog.getCatalog().getPageSize();
            int numPages = (int) Math.ceil((double) fileSize / pageSize);

            return numPages;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    /**
     * prints the tree. gets each
     */
//        public void printTree() {
//                if (this.root == null)
//                        return;
//
//                Queue<Integer> queue = new LinkedList<Integer>();
//                Queue<Integer> newLineQueue = new LinkedList<Integer>();
//                TreeNode currentNode = root;
//                int treeLevel = 0; // the current tree level
//                // add all the leftmost nodes to get the new lines
//                while (currentNode.pagePointers.size() != 0) {
//                        newLineQueue.add(currentNode.pageNumber);
//                        currentNode = BPlusTree.readNode(currentNode.pagePointers.get(0));
//                        treeLevel += 1;
//                }
//                newLineQueue.add(currentNode.pageNumber); // add the leaf node
//                newLineQueue.poll(); // remove the root so there is no \n in before the root
//
//                queue.add(root.pageNumber);
//                while (!queue.isEmpty()) { // later change to currentNode.isLeaf == false
//                        currentNode = BPlusTree.readNode(queue.poll());
//                        for (int i = 0; i < treeLevel; i++) {
//                                System.out.print("\t");
//                        }
//                        currentNode.printValues();
//
//                        queue.addAll(currentNode.pagePointers);
//                        if (queue.peek() == newLineQueue.peek()) {
//                                System.out.print("\n");
//                                newLineQueue.poll();
//                                treeLevel -= 1;
//                        } else {
//                                System.out.print("   ");
//                        }
//                }
//        }

    /**
     * prints the tree leafs (only the leafs)
     */
//        public void printLeafs() {
//                if (this.root == null)
//                        return;
//
//                TreeNode current = root;
//                while (!current.isLeaf) {
//                        current = BPlusTree.readNode(current.pagePointers.get(0));
//                }
//                while (current != null) {
//                        current.printValues();
//                        System.out.print(current.nextNode == null ? "\n" : " -> ");
//                        current = BPlusTree.readNode(current.nextNode);
//                }
//        }

    /**
     * alternative way of prints the tree leafs
     *
     *                     default = 0
     *                     available = [1]
     */
//        public void printLeafs(int formatOption) {
//                if (this.root == null)
//                        return;
//
//                TreeNode current = root;
//                while (!current.isLeaf) {
//                        current = BPlusTree.readNode(current.pagePointers.get(0));
//                }
//                while (current != null) {
//                        if (formatOption == 1) {
//                                current.parent.printValues();
//                                for (int i = 0; i < this.bucketSize - current.parent.searchKeys.size(); i++) {
//                                        System.out.print("\t");
//                                }
//                                System.out.print("=>\t");
//                                current.printValues();
//                                System.out.println();
//                        } else {
//                                current.printValues();
//                                System.out.print(current.nextNode == null ? "\n" : " -> ");
//                        }
//                        current = BPlusTree.readNode(current.nextNode);
//                }
//        }
    public static void main(String[] args) throws InvalidTypeException {
//                BPlusTree tree = new BPlusTree(5);
//
//                int [] inserts = {12, 10, 11, 12, 32, 15, 1};
//                AttributeType idType = new AttributeType("integer");
//                AttributeSchema idSchema = new AttributeSchema("id", idType, 0, true, true, false);
//                for (int num : inserts) {
//                        Attribute id = new Attribute(idSchema, num);
//                        System.out.println(tree.insert(id));
//                }
//                tree.printTree();
//
//                System.out.println("\nMore insertions");
//                int[] insertionValues = { 2, 4, 21, 17, 5, 6, 7, 8, 1, 10, 11, 12, 9, 14, 3, 16, 15, 13, 18, 20, 19, 22,
//                                23, 24, 25, 26, 27, 28, 29, 30, 31 };
//
//                for (int i = 0; i < insertionValues.length; i++) {
//                        Attribute id = new Attribute(idSchema, insertionValues[i]);
//                        tree.insert(id);
//                        tree.printTree();
//                        System.out.println();
//                }
//                System.out.println("\nLeaf Nodes:");
//                tree.printLeafs(1);
//
//                // delete testing
//                System.out.println("\nDelete testing");
//                int[] deleteValues = { 8, 9, 10, 11, 30, 31, 32, 21 };
//                for (int i = 0; i < deleteValues.length; i++) {
//                        System.out.println("Tree after deleting value \'" + deleteValues[i] + "\'");
//                        tree.delete(deleteValues[i]);
//                        tree.printTree();
//                        System.out.println();
//                }
//                System.out.println("\nLeaf Nodes:");
//                tree.printLeafs();
//                tree.printLeafs(1);
    }
}
