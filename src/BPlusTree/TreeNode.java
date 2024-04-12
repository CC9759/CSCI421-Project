package BPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeNode {
        private int bucketSize; // Size of the b+ tree to get size of nodes and separation
        public List<Integer> values;
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
         * function to find the TreeNode that is a leaf where the value is supposed to
         * be found. it's possible that the value is not on the node. this function is
         * used for insert and delete
         * 
         * @param value the value we are looking
         * @return the node where the value is supposed to be
         */
        public TreeNode find(int value) {
                if (this.isLeaf == true) {
                        return this;
                } else {
                        for (int i = 0; i < values.size(); i++) {
                                if (value < values.get(i)) {

                                }
                        }
                        return null;
                }
        }

        /**
         * function to insert a value in the B+ Tree
         * 
         * @param value the specified value that will be insert
         * @return a boolean. true if the value was succesfully added to the tree.
         *         otherwise, it returns false
         */
        public boolean insert(int value) {
                TreeNode node = find(value);
                if (node.values.contains(value))
                        return false;

                if (node.values.size() == this.bucketSize) {
                        divideNode(node);
                        node.values.add(value);
                } else {
                        node.values.add(value);
                }
                return true;
        }

        private void divideNode(TreeNode node) {
                // TODO: finish implementation
        }

        /**
         * function to delete a value in the B+ Tree
         * 
         * @param value the specified value that will be deleted
         * @return a boolean. true if the value was succesfully deleted from the tree.
         *         otherwise, it returns false
         */
        public boolean delete(int value) {
                // TODO: finish implementation
                return true;
        }

        public void printValues() {
                System.out.print(String.join(" | ",
                                values.stream().map(Object::toString).collect(Collectors.toUnmodifiableList())));
        }
}
