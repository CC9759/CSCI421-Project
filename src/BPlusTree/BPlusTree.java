package BPlusTree;

import java.util.LinkedList;
import java.util.Queue;

public class BPlusTree {
        private int bucketSize; // Size of the b+ tree to get size of nodes and separation
        private TreeNode root;

        public BPlusTree(int size) {
                this.bucketSize = size;
                this.root = new TreeNode(size);
                this.root.isLeaf = true;
        }

        /**
         * function to insert a value in the B+ Tree
         * 
         * @param value the specified value that will be insert
         * @return a boolean. true if the value was succesfully added to the tree.
         *         otherwise, it returns false
         */
        public boolean insert(int value) {
                return root.insert(value);
        }

        public boolean delete(int value) {
                return root.delete(value);
        }

        public TreeNode find(int value) {
                return root.find(value, root);
        }

        public int getTreeSize() {
                return bucketSize;
        }

        /**
         * prints the tree. gets each
         */
        public void printTree() {
                if (this.root == null)
                        return;

                Queue<TreeNode> queue = new LinkedList<>();
                queue.add(root);
                while (queue.size() != 0) {
                        queue.add(null);
                        TreeNode currentNode = queue.remove();
                        if (currentNode == null)
                                return;
                        queue.addAll(currentNode.keys);
                        currentNode.printValues();
                        System.out.print(queue.peek() == null ? "\n" : " -> ");
                        if (queue.peek() == null)
                                queue.remove();
                }
        }

        /**
         * prints the tree. gets each
         */
        public void printLeafs() {
                if (this.root == null)
                        return;

                TreeNode current = root;
                while (!current.isLeaf) {
                        current = current.keys.get(0);
                }
                while (current != null) {
                        current.printValues();
                        System.out.print(current.nextNode == null ? "\n" : " -> ");
                        current = current.nextNode;
                }
        }

        public static void main(String[] args) {
                BPlusTree tree = new BPlusTree(5);

                System.out.println(tree.insert(12));
                System.out.println(tree.insert(10));
                System.out.println(tree.insert(11));
                System.out.println(tree.insert(12)); // repeated value
                System.out.println(tree.insert(32));
                System.out.println(tree.insert(15));
                System.out.println(tree.insert(1));

                tree.printTree();

                System.out.println("\nMore insertions");

                int[] insertionValues = { 2, 4, 21, 17, 5, 6, 7, 8, 1, 10, 11, 12, 9, 14, 3, 16, 15, 13, 18, 20, 19 };
                for (int i = 0; i < insertionValues.length; i++) {
                        tree.insert(insertionValues[i]);
                        tree.printTree();
                        System.out.println();
                }

                tree.printTree();
                System.out.println("\nLeaf Nodes:");
                tree.printLeafs();
        }
}
