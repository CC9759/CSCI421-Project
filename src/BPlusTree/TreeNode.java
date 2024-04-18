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
                if (node.parent == null) { // if im dividing the root
                        TreeNode newNode1 = new TreeNode(bucketSize);
                        TreeNode newNode2 = new TreeNode(bucketSize);

                        int lowestNodeSize = (int) Math.ceil(node.values.size() / 2.0);
                        for (int i = 0; i < lowestNodeSize - 1; i++) {
                                // copy values in newNode1
                                newNode1.values.add(node.values.remove(0));
                        }
                        for (int i = 0; i < node.values.size(); i++) {
                                // copy remaining values in newNode2
                                newNode2.values.add(node.values.get(i));
                        }
                        node.values.clear();

                        newNode1.parent = node;
                        newNode1.isLeaf = node.isLeaf;
                        newNode2.parent = node;
                        newNode2.isLeaf = node.isLeaf;
                        if (node.isLeaf == true)
                                newNode1.nextNode = newNode2;
                        node.isLeaf = false;

                        // get copy the keys of node into new nodes
                        int keysDivision = node.keys.size() / 2;
                        for (int i = 0; i < keysDivision; i++) {
                                // copy keys in newNode1
                                node.keys.get(0).parent = newNode1;
                                newNode1.keys.add(node.keys.remove(0));
                        }
                        for (int i = 0; i < node.keys.size(); i++) {
                                // copy remaining keys in newNode2
                                node.keys.get(i).parent = newNode2;
                                newNode2.keys.add(node.keys.get(i));
                        }
                        node.keys.clear();

                        node.keys.add(newNode1);
                        node.keys.add(newNode2);
                        node.values.add(newNode2.values.get(0));
                        if (newNode2.isLeaf == false)
                                newNode2.values.remove(0);
                } else {
                        TreeNode newNode = new TreeNode(bucketSize);

                        int lowestNodeSize = (int) Math.ceil(node.values.size() / 2.0);
                        for (int i = 0; i < lowestNodeSize; i++) {
                                // copy values in newNode so:
                                // node.values (original) = node.values.addAll(newNode)
                                newNode.values.add(node.values.remove(lowestNodeSize - 1));
                        }

                        newNode.parent = node.parent;
                        newNode.isLeaf = node.isLeaf;

                        newNode.nextNode = node.nextNode; // have new node to point to potential nextNode
                        node.nextNode = newNode; // have node point to newNode

                        // copy the keys of node into newNode so: newNode = node[keyDiv:]
                        int keysDivision = node.keys.size() / 2;
                        for (int i = 0; i < keysDivision; i++) {
                                // copy keys in newNode
                                node.keys.get(keysDivision).parent = newNode;
                                newNode.keys.add(node.keys.remove(keysDivision));
                        }

                        // put newNode in the correct position for parent keys
                        node.parent.keys.add(node.parent.keys.indexOf(node) + 1, newNode);
                        // insert the value to the parent node
                        insertToNode(node.parent, newNode.values.get(0));
                        if (newNode.isLeaf == false) {
                                newNode.values.remove(0);
                                node.nextNode = null;
                                newNode.nextNode = null;
                        }

                        if (node.parent.values.size() == this.bucketSize)
                                divideNode(node.parent);
                }
        }

        /**
         * function to delete a value in the B+ Tree
         * 
         * 2 Cases for delete:
         * 1. The key to delete is only at the leaf node and not in the internal nodes
         * 2. The key to delete is both at the leaf node and in the internal nodes
         * 
         * @param value the specified value that will be deleted
         * @return a boolean. true if the value was succesfully deleted from the tree.
         *         otherwise, it returns false
         */
        public boolean delete(int value) {
                TreeNode node = find(value, this);

                if (!node.values.contains(value)) {
                        return false;
                }

                // if root, then just remove
                if (node.parent == null) {
                        node.values.remove(Integer.valueOf(value));
                }
                // if not root then delete and check for underfull
                else {
                        TreeNode currNode = node;
                        int originalNodeIndex = node.parent.keys.indexOf(node);

                        while (currNode != null) {
                                currNode.values.remove(Integer.valueOf(value));
                                
                                currNode = currNode.parent;
                        }

                        fixUnderfull(node, originalNodeIndex);
                }

                return true;
        }

        /**
         * Checks if the node has enough children/values and fixes it
         * 
         * @param node the current node to check and fix for underfull
         */
        private void fixUnderfull(TreeNode node, int originalNodeIndex) {
                TreeNode currNode = node;

                while (currNode.parent != null) {
                        if (currNode.isUnderfull()) {
                                int nodeIndex = currNode.parent.keys.indexOf(currNode);

                                boolean borrowSucess = borrowNodes(currNode, nodeIndex);
                                if (!borrowSucess) {
                                        mergeNodes(currNode, nodeIndex);
                                }
                        } else if (!currNode.isLeaf && currNode.isChildless()) {
                                int nodeIndex = currNode.parent.keys.indexOf(currNode);
                                mergeNodes(currNode, nodeIndex);
                        } else if (!currNode.isLeaf && currNode.values.size() < currNode.keys.size() - 1) {
                                insertToNode(currNode, node.parent.keys.get(originalNodeIndex).values.get(0));
                        }
                        currNode = currNode.parent;
                }

                // if the root is underfull, then borrow from the leaf node
                if (currNode.values.size() < currNode.keys.size() - 1) {
                        insertToNode(currNode, node.parent.keys.get(originalNodeIndex).values.get(0));
                }

                // if root and not enough children
                if (currNode.keys.size() < 2) {
                        if (node.keys.size() == 1) {
                                TreeNode onlyChild = node.keys.get(0);
                                node.keys = onlyChild.keys;
                                node.values = onlyChild.values;
                                node.parent = null;
                        } else {
                                node = null;
                        }
                }
        }

        /**
         * Tries to merge the current node to first the left node, then the right node
         * 
         * @param node the current node to be merged
         * @return whether the operation is successful
         */
        private boolean mergeNodes(TreeNode node, int nodeIndex) {
                // merge with left sibling
                if (nodeIndex > 0) {
                        TreeNode leftSibling = node.parent.keys.get(nodeIndex - 1);
                        leftSibling.values.addAll(node.values);

                        // inner node
                        if (node.nextNode == null) {
                                leftSibling.keys.addAll(node.keys);
                                for (TreeNode key : leftSibling.keys) {
                                        key.parent = leftSibling;
                                }
                        }
                        // if leaf node, then we gotta change the nextNode value of the left sibling
                        else if (node.isLeaf) {
                                leftSibling.nextNode = node.nextNode;
                        }

                        node.parent.keys.remove(nodeIndex);

                        return true;
                }
                // merge with right sibling
                else if (node.parent.keys.size() > 1 && nodeIndex < node.parent.keys.size() - 1) {
                        TreeNode rightSibling = node.parent.keys.get(nodeIndex + 1);
                        int rightSiblingOriginalNum = node.parent.values.indexOf(rightSibling.values.get(0));
                        node.values.addAll(rightSibling.values);
                        rightSibling.values = new ArrayList<>(node.values);

                        // inner node
                        if (node.nextNode == null) {
                                node.keys.addAll(rightSibling.keys);
                                rightSibling.keys = new ArrayList<>(node.keys);
                                for (TreeNode key : rightSibling.keys) {
                                        key.parent = rightSibling;
                                }
                        }
                        // if leaf node, then we gotta change the nextNode value of the left sibling
                        else if (node.isLeaf) {
                                if (nodeIndex - 1 >= 0) {
                                        node.parent.keys.get(nodeIndex - 1).nextNode = rightSibling;
                                }
                        }

                        if (rightSiblingOriginalNum != -1) {
                                node.parent.values.set(rightSiblingOriginalNum, rightSibling.values.get(0));
                        }

                        node.parent.keys.remove(nodeIndex);
                        return true;
                }

                return false;
        }

        /**
         * Tries to borrow values from first left and then right sibling
         * 
         * @param parent    the parent node of the current node
         * @param node      the current node that is borrowing
         * @param nodeIndex the index of the node
         * @return whether the operation is successful
         */
        private boolean borrowNodes(TreeNode node, int nodeIndex) {
                // try borrowing from left
                if (nodeIndex > 0 && node.parent.keys.get(nodeIndex - 1).values
                                .size() > (Math.ceil((double) this.bucketSize / 2.0) - 1)) {
                        TreeNode leftSibling = node.parent.keys.get(nodeIndex - 1);
                        int borrowedValue = leftSibling.values.remove(leftSibling.values.size() - 1);
                        insertToNode(node, borrowedValue);
                        node.parent.values.set(nodeIndex - 1, borrowedValue);
                        return true;
                }
                // try borrowing from right
                else if (node.parent.keys.size() > 1
                                && nodeIndex < node.parent.keys.size() - 1
                                && node.parent.keys.get(nodeIndex + 1).values
                                                .size() > (Math.ceil((double) this.bucketSize / 2.0) - 1)) {
                        TreeNode rightSibling = node.parent.keys.get(nodeIndex + 1);
                        int borrowedValue = rightSibling.values.remove(0);
                        insertToNode(node, borrowedValue);
                        node.parent.values.set(nodeIndex, borrowedValue);
                        return true;
                }

                return false;
        }

        /**
         * checks if the node has enough values
         * 
         * @return true or false if the node has enough values
         */
        private boolean isUnderfull() {
                if (this.parent == null)
                        return this.values.size() < 1;
                else
                        return this.values.size() < Math.ceil(((double) this.bucketSize) / 2.0) - 1;
        }

        /**
         * checks if the node has enough children
         * 
         * @return true or false if the node has enough children
         */
        private boolean isChildless() {
                return this.keys.size() < Math.floor((double) bucketSize / 2.0) + 1;
        }

        /**
         * function to print the values contained in a TreeNode
         */
        public void printValues() {
                System.out.print(String.join(" | ",
                                values.stream().map(Object::toString).collect(Collectors.toUnmodifiableList())));
        }

        public void writeNode() {

        }
}
