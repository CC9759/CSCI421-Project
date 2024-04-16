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

                Queue<TreeNode> queue = new LinkedList<TreeNode>();
                Queue<TreeNode> newLineQueue = new LinkedList<TreeNode>();
                TreeNode currentNode = root;
                int treeLevel = 0; // the current tree level
                // add all the leftmost nodes to get the new lines
                while (currentNode.keys.size() != 0) {
                        newLineQueue.add(currentNode);
                        currentNode = currentNode.keys.get(0);
                        treeLevel += 1;
                }
                newLineQueue.add(currentNode); // add the leaf node
                newLineQueue.poll(); // remove the root so there is no \n in before the root

                queue.add(root);
                while (!queue.isEmpty()) { // later change to currentNode.isLeaf == false
                        currentNode = queue.poll();
                        for (int i = 0; i < treeLevel; i++) {
                                System.out.print("\t");
                        }
                        currentNode.printValues();

                        queue.addAll(currentNode.keys);
                        if (queue.peek() == newLineQueue.peek()) {
                                System.out.print("\n");
                                newLineQueue.poll();
                                treeLevel -= 1;
                        } else {
                                System.out.print("   ");
                        }
                }
        }

        /**
         * prints the tree leafs (only the leafs)
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

        /**
         * alternative way of prints the tree leafs
         * 
         * @param formatOption the format in which to display the tree leafs.
         *                     default = 0
         *                     available = [1]
         */
        public void printLeafs(int formatOption) {
                if (this.root == null)
                        return;

                TreeNode current = root;
                while (!current.isLeaf) {
                        current = current.keys.get(0);
                }
                while (current != null) {
                        if (formatOption == 1) {
                                current.parent.printValues();
                                for (int i = 0; i < this.bucketSize - current.parent.values.size(); i++) {
                                        System.out.print("\t");
                                }
                                System.out.print("=>\t");
                                current.printValues();
                                System.out.println();
                        } else {
                                current.printValues();
                                System.out.print(current.nextNode == null ? "\n" : " -> ");
                        }
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
                int[] insertionValues = { 2, 4, 21, 17, 5, 6, 7, 8, 1, 10, 11, 12, 9, 14, 3, 16, 15, 13, 18, 20, 19, 22,
                                23, 24, 25, 26, 27, 28, 29, 30, 31 };
                for (int i = 0; i < insertionValues.length; i++) {
                        tree.insert(insertionValues[i]);
                        tree.printTree();
                        System.out.println();
                }
                System.out.println("\nLeaf Nodes:");
                tree.printLeafs();
                tree.printLeafs(1);

                // delete testing
                System.out.println("\nDelete testing");
                int[] deleteValues = { 8, 9, 10, 11, 30, 31, 32, 21 };
                for (int i = 0; i < deleteValues.length; i++) {
                        System.out.println("Tree after deleting value \'" + deleteValues[i] + "\'");
                        tree.delete(deleteValues[i]);
                        tree.printTree();
                        System.out.println();
                }
                System.out.println("\nLeaf Nodes:");
                tree.printLeafs();
                tree.printLeafs(1);
        }
}
