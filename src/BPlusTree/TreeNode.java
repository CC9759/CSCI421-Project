package BPlusTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeNode {
        private int bucketSize; // size of the b+ tree to get size of nodes and separation
        public List<Integer> values; // the current size of the node is node.values.size()
        public List<TreeNode> keys;
        public TreeNode nextNode; // null for all internal nodes and the root
        public TreeNode parent; // null for the root
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
        public TreeNode find(int value, TreeNode node) {
                if (node.isLeaf == true)
                        return node;
                for (int i = 0; i < node.values.size(); i++) {
                        if (value < node.values.get(i)) {
                                return find(value, node.keys.get(i));
                        }
                }
                return find(value, node.keys.get(node.values.size()));
        }

        /**
         * function to insert a value in the B+ Tree
         * 
         * @param value the specified value that will be insert
         * @return a boolean. true if the value was succesfully added to the tree.
         *         otherwise, it returns false
         */
        public boolean insert(int value) {
                TreeNode node = find(value, this);
                if (node.values.contains(value))
                        return false;

                if (node.values.size() + 1 == this.bucketSize) {
                        insertToNode(node, value);
                        divideNode(node);
                } else {
                        insertToNode(node, value);
                }
                return true;
        }

        private void insertToNode(TreeNode node, int value) {
                if (node.values.size() == 0) {
                        node.values.add(value);
                        return;
                } else if (node.values.size() == 1) {
                        if (value < node.values.get(0)) {
                                node.values.add(0, value);
                        } else {
                                node.values.add(value);
                        }
                        return;
                }
                for (int i = 0; i < node.values.size(); i++) {
                        if (value < node.values.get(i)) {
                                node.values.add(i, value);
                                return;
                        }
                }
                node.values.add(value); // if its the largest value in the node
        }

        private void divideNode(TreeNode node) {
                TreeNode newNode1 = new TreeNode(bucketSize);
                TreeNode newNode2 = new TreeNode(bucketSize);

                int lowestNodeSize = (int) Math.ceil(node.values.size() / 2.0);
                for (int i = 0; i < lowestNodeSize - 1; i++) {
                        // copy keys and values in newNode1
                        newNode1.values.add(node.values.remove(0));
                }
                for (int i = 0; i < node.values.size(); i++) {
                        // copy remaining keys and values in newNode2
                        newNode2.values.add(node.values.get(i));
                }
                node.values.clear();

                if (node.parent == null) {
                        newNode1.parent = node;
                        newNode1.isLeaf = true;
                        newNode2.parent = node;
                        newNode2.isLeaf = true;

                        newNode1.nextNode = newNode2;

                        node.keys.add(newNode1);
                        node.keys.add(newNode2);
                        node.isLeaf = false;
                        node.values.add(newNode2.values.get(0));
                } else {
                        newNode1.parent = node.parent;
                        newNode1.isLeaf = node.isLeaf;
                        newNode2.parent = node.parent;
                        newNode2.isLeaf = node.isLeaf;

                        newNode1.nextNode = newNode2;
                        newNode2.nextNode = node.nextNode;

                        int pointer = node.parent.keys.indexOf(node); // the pointer where new nodes belong
                        node.parent.keys.remove(node);

                        node.parent.keys.add(pointer, newNode2);
                        node.parent.keys.add(pointer, newNode1);
                        if (!node.parent.values.contains(newNode2.values.get(0)))
                                node.parent.values.add(pointer, newNode2.values.get(0));
                        else if (!node.parent.values.contains(newNode1.values.get(0)))
                                node.parent.values.add(pointer, newNode1.values.get(0));

                        if (node.parent.values.size() == this.bucketSize)
                                divideInternal(node.parent);
                }
        }

        private void divideInternal(TreeNode node) {
                TreeNode newNode1 = new TreeNode(bucketSize);
                TreeNode newNode2 = new TreeNode(bucketSize);

                int lowestNodeSize = (int) Math.ceil(node.values.size() / 2.0);
                for (int i = 0; i < lowestNodeSize - 1; i++) {
                        // copy keys and values in newNode1
                        newNode1.values.add(node.values.remove(0));
                }
                for (int i = 0; i < lowestNodeSize - 1; i++) {
                        // copy remaining keys and values in newNode2
                        newNode2.values.add(node.values.remove(1));
                }

                // give keys to nodes
                int split = node.keys.size() / 2; // split the pointers
                for (int i = 0; i < split; i++) {
                        newNode1.keys.add(node.keys.remove(0));
                }
                for (int i = 0; i < split; i++) {
                        newNode2.keys.add(node.keys.remove(0));
                }

                if (node.parent == null) {
                        newNode1.parent = node;
                        newNode1.isLeaf = false;
                        newNode2.parent = node;
                        newNode2.isLeaf = false;

                        node.keys.add(newNode1);
                        node.keys.add(newNode2);
                        node.isLeaf = false;
                } else {
                        newNode1.parent = node.parent;
                        newNode1.isLeaf = node.isLeaf;
                        newNode2.parent = node.parent;
                        newNode2.isLeaf = node.isLeaf;

                        int pointer = node.parent.keys.indexOf(node); // the pointer where new nodes belong
                        node.parent.keys.remove(node);

                        node.parent.keys.add(pointer, newNode2);
                        node.parent.keys.add(pointer, newNode1);
                        if (!node.parent.values.contains(newNode2.values.get(0)))
                                node.parent.values.add(pointer, newNode2.values.get(0));
                        else if (!node.parent.values.contains(newNode1.values.get(0)))
                                node.parent.values.add(pointer, newNode1.values.get(0));

                        if (node.parent.values.size() == this.bucketSize)
                                divideInternal(node.parent);
                }
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
