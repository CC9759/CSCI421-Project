package BPlusTree;

import storageManager.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeNode {
        private final int bucketSize; // size of the b+ tree to get size of nodes and separation
        public List<Attribute> searchKeys; // The index in the array the current size of the node is node.values.size()
        public List<TreeNode> pagePointers; // the primary key attribute
        public TreeNode nextNode; // null for all internal nodes and the root
        public TreeNode parent; // null for the root
        public boolean isLeaf;
        public int pageNumber;

        // Node constructor
        public TreeNode(int size) {
                this.bucketSize = size;
                this.searchKeys = new ArrayList<>();
                this.pagePointers = new ArrayList<>();
                this.nextNode = null;
                this.parent = null;
                this.isLeaf = false;
                this.pageNumber = 0;
        }

        /**
         * function to find the TreeNode that is a leaf where the value is supposed to
         * be found. it's possible that the value is not on the node. this function is
         * used for insert and delete
         * 
         * @param value the value we are looking
         * @return the node where the value is supposed to be
         */
        public TreeNode find(Object value, TreeNode node) {
                if (node.isLeaf)
                        return node;
                for (int i = 0; i < node.searchKeys.size(); i++) {
                        if (Attribute.compareTo(value, node.searchKeys.get(i).getData()) < 0) {
                                return find(value, node.pagePointers.get(i));
                        }
                }
                return find(value, node.pagePointers.get(node.searchKeys.size()));
        }

        /**
         * function to insert a value in the B+ Tree
         * 
         * @param value the specified value that will be insert
         * @return a boolean. true if the value was succesfully added to the tree.
         *         otherwise, it returns false
         */
        public boolean insert(Attribute value) {
                TreeNode node = find(value.getData(), this);

                if (node.contains(value.getData())) {
                        return false;
                }

                if (node.searchKeys.size() + 1 == this.bucketSize) {
                        insertToNode(node, value);
                        divideNode(node);
                } else {
                        insertToNode(node, value);
                }
                return true;
        }

        private void insertToNode(TreeNode node, Attribute value) {
                if (node.searchKeys.size() == 0) {
                        node.searchKeys.add(value);
                        return;
                } else if (node.searchKeys.size() == 1) {
                        if (value.compareTo(node.searchKeys.get(0)) <  0) {
                                node.searchKeys.add(0, value);
                        } else {
                                node.searchKeys.add(value);
                        }
                        return;
                }
                for (int i = 0; i < node.searchKeys.size(); i++) {
                        if (value.compareTo(node.searchKeys.get(i)) <  0) {
                                node.searchKeys.add(i, value);
                                return;
                        }
                }
                node.searchKeys.add(value); // if its the largest value in the node
        }

        private void divideNode(TreeNode node) {
                if (node.parent == null) { // if im dividing the root
                        TreeNode newNode1 = new TreeNode(bucketSize);
                        TreeNode newNode2 = new TreeNode(bucketSize);

                        int lowestNodeSize = (int) Math.ceil(node.searchKeys.size() / 2.0);
                        for (int i = 0; i < lowestNodeSize - 1; i++) {
                                // copy values in newNode1
                                newNode1.searchKeys.add(node.searchKeys.remove(0));
                        }
                        for (int i = 0; i < node.searchKeys.size(); i++) {
                                // copy remaining values in newNode2
                                newNode2.searchKeys.add(node.searchKeys.get(i));
                        }
                        node.searchKeys.clear();

                        newNode1.parent = node;
                        newNode1.isLeaf = node.isLeaf;
                        newNode2.parent = node;
                        newNode2.isLeaf = node.isLeaf;
                        if (node.isLeaf)
                                newNode1.nextNode = newNode2;
                        node.isLeaf = false;

                        // get copy the keys of node into new nodes
                        int keysDivision = node.pagePointers.size() / 2;
                        for (int i = 0; i < keysDivision; i++) {
                                // copy keys in newNode1
                                node.pagePointers.get(0).parent = newNode1;
                                newNode1.pagePointers.add(node.pagePointers.remove(0));
                        }
                        for (int i = 0; i < node.pagePointers.size(); i++) {
                                // copy remaining keys in newNode2
                                node.pagePointers.get(i).parent = newNode2;
                                newNode2.pagePointers.add(node.pagePointers.get(i));
                        }
                        node.pagePointers.clear();

                        node.pagePointers.add(newNode1);
                        node.pagePointers.add(newNode2);
                        node.searchKeys.add(newNode2.searchKeys.get(0));
                        if (!newNode2.isLeaf)
                                newNode2.searchKeys.remove(0);
                } else {
                        TreeNode newNode = new TreeNode(bucketSize);

                        int lowestNodeSize = (int) Math.ceil(node.searchKeys.size() / 2.0);
                        for (int i = 0; i < lowestNodeSize; i++) {
                                // copy values in newNode so:
                                // node.values (original) = node.values.addAll(newNode)
                                newNode.searchKeys.add(node.searchKeys.remove(lowestNodeSize - 1));
                        }

                        newNode.parent = node.parent;
                        newNode.isLeaf = node.isLeaf;

                        newNode.nextNode = node.nextNode; // have new node to point to potential nextNode
                        node.nextNode = newNode; // have node point to newNode

                        // copy the keys of node into newNode so: newNode = node[keyDiv:]
                        int keysDivision = node.pagePointers.size() / 2;
                        for (int i = 0; i < keysDivision; i++) {
                                // copy keys in newNode
                                node.pagePointers.get(keysDivision).parent = newNode;
                                newNode.pagePointers.add(node.pagePointers.remove(keysDivision));
                        }

                        // put newNode in the correct position for parent keys
                        //node.parent.keys.add(node.parent.keys.indexOf(node) + 1, newNode);
                        node.parent.pagePointers.add(node.parent.getKeyIndex(node) + 1, newNode);
                        // insert the value to the parent node
                        insertToNode(node.parent, newNode.searchKeys.get(0));
                        if (!newNode.isLeaf) {
                                newNode.searchKeys.remove(0);
                                node.nextNode = null;
                                newNode.nextNode = null;
                        }

                        if (node.parent.searchKeys.size() == this.bucketSize)
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
        public boolean delete(Object value) {
                TreeNode node = find(value, this);

                if (!node.contains(value)) {
                        return false;
                }

                // if root, then just remove
                if (node.parent == null) {
                        int index = getValueIndex(value);
                        if (index > -1) {
                                node.searchKeys.remove(index);
                        }
                }
                // if not root then delete and check for underfull
                else {
                        TreeNode currNode = node;
                        // take a look at this
                        int originalNodeIndex = node.parent.getKeyIndex(node);

                        while (currNode != null) {
                                int index = currNode.getValueIndex(value);
                                if (index > -1) {
                                        currNode.searchKeys.remove(index);

                                }
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
                                int nodeIndex = currNode.parent.getKeyIndex(currNode);

                                boolean borrowSucess = borrowNodes(currNode, nodeIndex);
                                if (!borrowSucess) {
                                        mergeNodes(currNode, nodeIndex);
                                }
                        } else if (!currNode.isLeaf && currNode.isChildless()) {
                                int nodeIndex = currNode.parent.getKeyIndex(currNode);
                                mergeNodes(currNode, nodeIndex);
                        } else if (!currNode.isLeaf && currNode.searchKeys.size() < currNode.pagePointers.size() - 1) {
                                insertToNode(currNode, node.parent.pagePointers.get(originalNodeIndex).searchKeys.get(0));
                        }
                        currNode = currNode.parent;
                }

                // if the root is underfull, then borrow from the leaf node
                if (currNode.searchKeys.size() < currNode.pagePointers.size() - 1) {
                        insertToNode(currNode, node.parent.pagePointers.get(originalNodeIndex).searchKeys.get(0));
                }

                // if root and not enough children
                if (currNode.pagePointers.size() < 2) {
                        if (node.pagePointers.size() == 1) {
                                TreeNode onlyChild = node.pagePointers.get(0);
                                node.pagePointers = onlyChild.pagePointers;
                                node.searchKeys = onlyChild.searchKeys;
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
                        TreeNode leftSibling = node.parent.pagePointers.get(nodeIndex - 1);
                        leftSibling.searchKeys.addAll(node.searchKeys);

                        // inner node
                        if (node.nextNode == null) {
                                leftSibling.pagePointers.addAll(node.pagePointers);
                                for (TreeNode key : leftSibling.pagePointers) {
                                        key.parent = leftSibling;
                                }
                        }
                        // if leaf node, then we gotta change the nextNode value of the left sibling
                        else if (node.isLeaf) {
                                leftSibling.nextNode = node.nextNode;
                        }

                        node.parent.pagePointers.remove(nodeIndex);

                        return true;
                }
                // merge with right sibling
                else if (node.parent.pagePointers.size() > 1 && nodeIndex < node.parent.pagePointers.size() - 1) {
                        TreeNode rightSibling = node.parent.pagePointers.get(nodeIndex + 1);
                        int rightSiblingOriginalNum = node.parent.searchKeys.indexOf(rightSibling.searchKeys.get(0));
                        node.searchKeys.addAll(rightSibling.searchKeys);
                        rightSibling.searchKeys = new ArrayList<>(node.searchKeys);

                        // inner node
                        if (node.nextNode == null) {
                                node.pagePointers.addAll(rightSibling.pagePointers);
                                rightSibling.pagePointers = new ArrayList<>(node.pagePointers);
                                for (TreeNode key : rightSibling.pagePointers) {
                                        key.parent = rightSibling;
                                }
                        }
                        // if leaf node, then we gotta change the nextNode value of the left sibling
                        else if (node.isLeaf) {
                                if (nodeIndex - 1 >= 0) {
                                        node.parent.pagePointers.get(nodeIndex - 1).nextNode = rightSibling;
                                }
                        }

                        if (rightSiblingOriginalNum != -1) {
                                node.parent.searchKeys.set(rightSiblingOriginalNum, rightSibling.searchKeys.get(0));
                        }

                        node.parent.pagePointers.remove(nodeIndex);
                        return true;
                }

                return false;
        }

        /**
         * Tries to borrow values from first left and then right sibling
         *
         * @param node      the current node that is borrowing
         * @param nodeIndex the index of the node
         * @return whether the operation is successful
         */
        private boolean borrowNodes(TreeNode node, int nodeIndex) {
                // try borrowing from left
                if (nodeIndex > 0 && node.parent.pagePointers.get(nodeIndex - 1).searchKeys
                                .size() > (Math.ceil((double) this.bucketSize / 2.0) - 1)) {
                        TreeNode leftSibling = node.parent.pagePointers.get(nodeIndex - 1);
                        Attribute borrowedValue = leftSibling.searchKeys.remove(leftSibling.searchKeys.size() - 1);
                        insertToNode(node, borrowedValue);
                        node.parent.searchKeys.set(nodeIndex - 1, borrowedValue);
                        return true;
                }
                // try borrowing from right
                else if (node.parent.pagePointers.size() > 1
                                && nodeIndex < node.parent.pagePointers.size() - 1
                                && node.parent.pagePointers.get(nodeIndex + 1).searchKeys
                                                .size() > (Math.ceil((double) this.bucketSize / 2.0) - 1)) {
                        TreeNode rightSibling = node.parent.pagePointers.get(nodeIndex + 1);
                        Attribute borrowedValue = rightSibling.searchKeys.remove(0);
                        insertToNode(node, borrowedValue);
                        node.parent.searchKeys.set(nodeIndex, borrowedValue);
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
                        return this.searchKeys.size() < 1;
                else
                        return this.searchKeys.size() < Math.ceil(((double) this.bucketSize) / 2.0) - 1;
        }

        /**
         * checks if the node has enough children
         * 
         * @return true or false if the node has enough children
         */
        private boolean isChildless() {
                return this.pagePointers.size() < Math.floor((double) bucketSize / 2.0) + 1;
        }

        /**
         * function to print the values contained in a TreeNode
         */
        public void printValues() {
                System.out.print(String.join(" | ",
                                searchKeys.stream().map(Object::toString).collect(Collectors.toUnmodifiableList())));
        }

        public void writeNode() {

        }

        private boolean contains(Object attribute) {
                for (Attribute attr : this.searchKeys) {
                        if (Attribute.compareTo(attr.getData(), attribute) == 0) {
                                return true;
                        }
                }
                return false;
        }

        private int getValueIndex(Object value) {
                for (int i = 0; i < this.searchKeys.size(); i++) {
                        if (Attribute.compareTo(value, this.searchKeys.get(i).getData()) == 0) {
                                return i;
                        }
                }
                return -1;
        }
        private int getKeyIndex(TreeNode node) {
                for (int i = 0; i < this.pagePointers.size(); i++) {
                        if (pagePointers.get(i) == node) {
                                return i;
                        }
                }
                return -1;
        }
}
