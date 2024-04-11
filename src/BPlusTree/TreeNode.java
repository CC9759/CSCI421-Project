package BPlusTree;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
        private int bucketSize; // Size of the b+ tree to get size of nodes and separation
        public List<String> values;
        public List<TreeNode> keys;
        public TreeNode nextNode;
        public TreeNode parent;
        public boolean isLeaf;

        // Node constructor
        public TreeNode(int size) {
                this.bucketSize = size;
                this.values = new ArrayList<>();
                this.keys = new ArrayList<>();
                this.nextNode = null;
                this.parent = null;
                this.isLeaf = false;
        }

        /**
         * function to insert a value in the B+ Tree
         * 
         * @param value the specified value that will be insert
         * @return a boolean. true if the value was succesfully added to the tree.
         *         otherwise, it returns false
         */
        public boolean insert(String value) {
                // TODO: finish implementation
                return true;
        }

        // Delete

        public void printValues() {
                System.out.print(String.join(" | ", values));
        }
}
