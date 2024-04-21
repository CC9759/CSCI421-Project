package BPlusTree;

import Exceptions.IllegalOperationException;
import catalog.Catalog;
import storageManager.Attribute;
import storageManager.Table;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// These are index pages in hardware
public class TreeNode {
        private List<Attribute> searchKeys; // The index in the array the current size of the node is node.values.size()
        private List<Index> indices;
        public int nextNode; // null for all internal nodes and the root
        public int parent; // null for the root
        public boolean isLeaf;
        public int nodeNumber;
        private int freeSpaceAmount;
        private Table table;
        private boolean wasUpdated;

        // Node constructor
        public TreeNode(Table table, boolean isLeaf) {
                this.table = table;
                this.searchKeys = new ArrayList<>();
                this.indices = new ArrayList<>();
                this.nextNode = -1;
                this.parent = -1;
                this.isLeaf = isLeaf;
                this.nodeNumber = table.root.getNumNodes();
                table.root.setNumNodes(table.root.getNumNodes() + 1);
                this.update();
        }

        public TreeNode(Table table, int nodeNumber, boolean isLeaf) {
                this.table = table;
                this.searchKeys = new ArrayList<>();
                this.indices = new ArrayList<>();
                this.nextNode = -1;
                this.parent = -1;
                this.isLeaf = isLeaf;
                this.nodeNumber = nodeNumber;
                this.update();
        }

        /**
         * function to find the TreeNode that is a leaf where the value is supposed to
         * be found. it's possible that the value is not on the node. this function is
         * used for insert and delete
         *
         * @param value the value we are looking
         * @return the node where the value is supposed to be
         */
        public TreeNode find(Object value, Integer pageNumber) throws IllegalOperationException {
                TreeNode node = table.readNode(pageNumber);
                if (node.isLeaf)
                        return node;
                for (int i = 0; i < node.searchKeys.size(); i++) {
                        if (Attribute.compareTo(value, node.searchKeys.get(i).getData()) < 0) {
                                return find(value, node.indices.get(i).pageNumber);
                        }
                }
                return find(value, node.indices.get(node.searchKeys.size()).pageNumber);
        }

        /**
         * function to insert a value in the B+ Tree
         *
         * @param value the specified value that will be insert
         * @return treenode which has the insert
         */
        public TreeNode insert(Attribute value) throws IllegalOperationException {
                TreeNode node = find(value.getData(), this.nodeNumber);

                if (node.contains(value.getData())) {
                        return null;
                }

                if (node.searchKeys.size() + 1 == this.table.root.N) {

                        insertToNode(node, value);
                        System.out.println("Div root");
                        divideNode(node);
                } else {
                        insertToNode(node, value);
                }
                node.writeNode();
                return node;
        }

        private void insertToNode(TreeNode node, Attribute value) {
                if (node.searchKeys.size() == 0) {
                        node.addKey(value);
                        return;
                } else if (node.searchKeys.size() == 1) {
                        if (value.compareTo(node.searchKeys.get(0)) < 0) {
                                node.addKey(0, value);
                        } else {
                                node.addKey(value);
                        }
                        return;
                }
                for (int i = 0; i < node.searchKeys.size(); i++) {
                        if (value.compareTo(node.searchKeys.get(i)) < 0) {
                                node.addKey(i, value);
                                return;
                        }
                }
                node.addKey(value); // if its the largest value in the node
                node.writeNode();
        }

        private void divideNode(TreeNode node) throws IllegalOperationException {
                if (node.parent == -1) { // if im dividing the root
                        TreeNode newNode1 = new TreeNode(table, isLeaf);
                        TreeNode newNode2 = new TreeNode(table, isLeaf);

                        int lowestNodeSize = (int) Math.ceil(node.searchKeys.size() / 2.0);
                        for (int i = 0; i < lowestNodeSize - 1; i++) {
                                // copy values in newNode1
                                newNode1.addKey(node.removeKey(0));
                        }
                        for (int i = 0; i < node.searchKeys.size(); i++) {
                                // copy remaining values in newNode2
                                newNode2.addKey(node.searchKeys.get(i));
                        }
                        node.searchKeys.clear();

                        newNode1.parent = node.nodeNumber;
                        newNode2.parent = node.nodeNumber;
                        if (node.isLeaf)
                                newNode1.nextNode = newNode2.nodeNumber;
                        node.isLeaf = false;

                        // get copy the pointers of node into new nodes
                        int keysDivision = node.indices.size() / 2;
                        for (int i = 0; i < keysDivision; i++) {
                                // copy pointers in newNode1
                                TreeNode child = table.readNode(node.indices.get(0).pageNumber);
                                child.parent = newNode1.nodeNumber;
                                child.writeNode();
                                newNode1.addIndex(node.removeIndex(0));;
                        }
                        for (int i = 0; i < node.indices.size(); i++) {
                                // copy remaining keys in newNode2
                                TreeNode child = table.readNode(node.indices.get(i).pageNumber);
                                child.parent = newNode2.nodeNumber;
                                child.writeNode();
                                newNode2.addIndex(node.indices.get(i));
                        }
                        node.indices.clear();

                        node.addIndex(new Index(newNode1.nodeNumber, -1));
                        node.addIndex(new Index(newNode2.nodeNumber, -1));
                        node.addKey(newNode2.searchKeys.get(0));
                        if (!newNode2.isLeaf) {
                                newNode2.searchKeys.remove(0);
                        }
                        newNode1.writeNode();
                        newNode2.writeNode();
                        node.writeNode();

                } else {
                        TreeNode newNode = new TreeNode(table, isLeaf);

                        int lowestNodeSize = (int) Math.ceil(node.searchKeys.size() / 2.0);
                        for (int i = 0; i < lowestNodeSize; i++) {
                                // copy values in newNode so:
                                // node.values (original) = node.values.addAll(newNode)
                                newNode.addKey(node.removeKey(lowestNodeSize - 1));
                        }

                        newNode.parent = node.parent;
                        newNode.isLeaf = node.isLeaf;

                        newNode.nextNode = node.nextNode; // have new node to point to potential nextNode
                        node.nextNode = newNode.nodeNumber; // have node point to newNode

                        // copy the keys of node into newNode so: newNode = node[keyDiv:]
                        int keysDivision = node.indices.size() / 2;
                        for (int i = 0; i < keysDivision; i++) {
                                // copy keys in newNode
                                TreeNode child = table.readNode(node.indices.get(keysDivision).pageNumber);
                                child.parent = newNode.nodeNumber;
                                child.writeNode();
                                newNode.addIndex(node.removeIndex(keysDivision));
                        }

                        // put newNode in the correct position for parent keys
                        //node.parent.keys.add(node.parent.keys.indexOf(node) + 1, newNode);
                        TreeNode parent = table.readNode(node.parent);
                        parent.addIndex(parent.getKeyIndex(node) + 1, new Index(newNode.nodeNumber, -1));
                        parent.writeNode();
                        // insert the value to the parent node
                        insertToNode(parent, newNode.searchKeys.get(0));
                        if (!newNode.isLeaf) {
                                newNode.searchKeys.remove(0);
                                node.nextNode = -1;
                                newNode.nextNode = -1;
                        }
                        node.writeNode();
                        newNode.writeNode();

                        if (parent.searchKeys.size() == this.table.root.N)
                                divideNode(parent);
                }
        }

        /**
         * function to delete a value in the B+ Tree
         * <p>
         * 2 Cases for delete:
         * 1. The key to delete is only at the leaf node and not in the internal nodes
         * 2. The key to delete is both at the leaf node and in the internal nodes
         *
         * @param value the specified value that will be deleted
         * @return a boolean. true if the value was succesfully deleted from the tree.
         * otherwise, it returns false
         */
        public boolean delete(Object value) throws IllegalOperationException {
                TreeNode node = find(value, this.nodeNumber);

                if (!node.contains(value)) {
                        return false;
                }

                // if root, then just remove
                if (node.parent == -1) {
                        int index = getValueIndex(value);
                        if (index > -1) {
                                node.removeKey(index);
                                node.writeNode();
                        }
                }
                // if not root then delete and check for underfull
                else {
                        TreeNode currNode = node;

                        TreeNode parent = table.readNode(nodeNumber);
                        int originalNodeIndex = parent.getKeyIndex(node);

                        while (currNode != null) {
                                int index = currNode.getValueIndex(value);
                                if (index > -1) {
                                        currNode.removeKey(index);
                                        currNode.writeNode();
                                }
                                TreeNode curParent = table.readNode(currNode.parent);
                                currNode = curParent;
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
        private void fixUnderfull(TreeNode node, int originalNodeIndex) throws IllegalOperationException {
                TreeNode currNode = node;

                while (currNode.parent != -1) {
                        TreeNode parent = table.readNode(currNode.parent);
                        if (currNode.isUnderfull()) {
                                int nodeIndex = parent.getKeyIndex(currNode);
                                boolean borrowSucess = borrowNodes(currNode, nodeIndex);
                                if (!borrowSucess) {
                                        mergeNodes(currNode, nodeIndex);
                                }
                        } else if (!currNode.isLeaf && currNode.isChildless()) {
                                int nodeIndex = parent.getKeyIndex(currNode);
                                mergeNodes(currNode, nodeIndex);
                        } else if (!currNode.isLeaf && currNode.searchKeys.size() < currNode.indices.size() - 1) {
                                TreeNode toInsert = table.readNode(parent.indices.get(originalNodeIndex).pageNumber);
                                insertToNode(currNode, toInsert.searchKeys.get(0));
                        }
                        currNode = parent;
                }

                // if the root is underfull, then borrow from the leaf node
                if (currNode.searchKeys.size() < currNode.indices.size() - 1) {
                        TreeNode parent = table.readNode(node.parent);
                        TreeNode toInsert = table.readNode(parent.indices.get(originalNodeIndex).pageNumber);
                        insertToNode(currNode, toInsert.searchKeys.get(0));
                }

                // if root and not enough children
                if (currNode.indices.size() < 2) {
                        if (node.indices.size() == 1) {
                                TreeNode onlyChild = table.readNode(node.indices.get(0).pageNumber);
                                node.indices = onlyChild.indices;
                                node.searchKeys = onlyChild.searchKeys;
                                node.parent = -1;
                                node.writeNode();
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
        private boolean mergeNodes(TreeNode node, int nodeIndex) throws IllegalOperationException {
                // merge with left sibling
                TreeNode parent = table.readNode(node.parent);
                if (nodeIndex > 0) {
                        TreeNode leftSibling = table.readNode(parent.indices.get(nodeIndex - 1).pageNumber);
                        leftSibling.addAllKeys(node.searchKeys);

                        // inner node
                        if (node.nextNode == -1) {
                                leftSibling.addAllIndices(node.indices);
                                for (Index pointer : leftSibling.indices) {
                                        TreeNode child = table.readNode(pointer.pageNumber);
                                        child.parent = leftSibling.nodeNumber;
                                        child.writeNode();
                                }
                        }
                        // if leaf node, then we gotta change the nextNode value of the left sibling
                        else if (node.isLeaf) {
                                leftSibling.nextNode = node.nextNode;
                        }
                        leftSibling.writeNode();

                        parent.removeIndex(nodeIndex);
                        parent.writeNode();

                        return true;
                }
                // merge with right sibling
                else if (parent.indices.size() > 1 && nodeIndex < parent.indices.size() - 1) {
                        TreeNode rightSibling = table.readNode(parent.indices.get(nodeIndex + 1).pageNumber);
                        int rightSiblingOriginalNum = parent.searchKeys.indexOf(rightSibling.searchKeys.get(0));
                        node.addAllKeys(rightSibling.searchKeys);
                        rightSibling.clearKeys();

                        // inner node
                        if (node.nextNode == -1) {
                                node.addAllIndices(rightSibling.indices);
                                rightSibling.clearIndices();
                                for (Index pointer : node.indices) {
                                        TreeNode child = table.readNode(pointer.pageNumber);
                                        child.parent = rightSibling.nodeNumber;
                                        child.writeNode();
                                }
                        }
                        // if leaf node, then we gotta change the nextNode value of the left sibling
                        else if (node.isLeaf) {
                                if (nodeIndex - 1 >= 0) {
                                        TreeNode nextNode = table.readNode(parent.indices.get(nodeIndex - 1).pageNumber);
                                        nextNode.nextNode = rightSibling.nodeNumber;
                                        nextNode.writeNode();
                                }
                        }

                        if (rightSiblingOriginalNum != -1) {
                                parent.searchKeys.set(rightSiblingOriginalNum, rightSibling.searchKeys.get(0));
                        }
                        rightSibling.writeNode();

                        parent.removeIndex(nodeIndex);
                        parent.writeNode();
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
        private boolean borrowNodes(TreeNode node, int nodeIndex) throws IllegalOperationException {
                // try borrowing from left
                TreeNode parent = table.readNode(node.parent);
                TreeNode leftSibling = table.readNode(parent.indices.get(nodeIndex - 1).pageNumber);
                TreeNode rightSibling = table.readNode(parent.indices.get(nodeIndex + 1).pageNumber);
                if (nodeIndex > 0 && leftSibling.searchKeys
                        .size() > (Math.ceil((double) this.table.root.N / 2.0) - 1)) {
                        Attribute borrowedValue = leftSibling.searchKeys.remove(leftSibling.searchKeys.size() - 1);
                        leftSibling.writeNode();
                        insertToNode(node, borrowedValue);
                        parent.searchKeys.set(nodeIndex - 1, borrowedValue);
                        parent.writeNode();
                        return true;
                }
                // try borrowing from right
                else if (parent.indices.size() > 1
                        && nodeIndex < parent.indices.size() - 1
                        && rightSibling.searchKeys
                        .size() > (Math.ceil((double) this.table.root.N / 2.0) - 1)) {
                        Attribute borrowedValue = rightSibling.searchKeys.remove(0);
                        rightSibling.writeNode();
                        insertToNode(node, borrowedValue);
                        parent.searchKeys.set(nodeIndex, borrowedValue);
                        parent.writeNode();
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
                if (this.parent == -1)
                        return this.searchKeys.size() < 1;
                else
                        return this.searchKeys.size() < Math.ceil(((double) this.table.root.N) / 2.0) - 1;
        }

        /**
         * checks if the node has enough children
         *
         * @return true or false if the node has enough children
         */
        private boolean isChildless() {
                return this.indices.size() < Math.floor((double) this.table.root.N / 2.0) + 1;
        }

        /**
         * function to print the values contained in a TreeNode
         */
        public void printValues() {
                System.out.print(String.join(" | ",
                        searchKeys.stream().map(Object::toString).collect(Collectors.toUnmodifiableList())));
        }

        public void writeNode() {
                if (!this.wasUpdated) return;
                try {
                        String location = table.schema.getNodeLocation();
                        Catalog catalog = Catalog.getCatalog();
                        RandomAccessFile file = new RandomAccessFile(location, "rw");
                        long offset = getNodeNumber() * Catalog.getCatalog().getPageSize();
                        byte[] nodeData = serializeNode();
                        if (nodeData.length != catalog.getPageSize()) {
                                throw new IllegalOperationException("Tried to write page of size " + nodeData.length + " bytes which is not the defined page size");
                        }
                        file.seek(offset);
                        file.write(nodeData);
                        file.close();
                } catch (IOException | IllegalOperationException error) {
                        System.err.println(error.getMessage());
                }
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
                for (int i = 0; i < this.indices.size(); i++) {
                        if (indices.get(i).pageNumber == node.nodeNumber) {
                                return i;
                        }
                }
                return -1;
        }

        public void addIndex(Index index) {
                indices.add(index);
                update();
        }

        public void addIndex(int spot, Index index) {
                indices.add(spot, index);
                update();
        }

        public void addAllIndices(List<Index> indices) {
                this.indices.addAll(indices);
                update();
        }


        public Index removeIndex(int index) {
                Index removed = indices.remove(index);
                update();
                return removed;
        }

        public void clearIndices() {
                indices.clear();
                update();
        }

        public void addKey(Attribute searchKey) {
                searchKeys.add(searchKey);
                update();
        }

        public void addKey(int spot, Attribute searchKey) {
                searchKeys.add(spot, searchKey);
                update();
        }

        public Attribute removeKey(int index) {
                Attribute removed = searchKeys.remove(index);
                update();
                return removed;
        }

        public void clearKeys() {
                searchKeys.clear();
                update();
        }

        public void addAllKeys(List<Attribute> searchKeys) {
                this.searchKeys.addAll(searchKeys);
                update();
        }



        public void calculateFreeSpace() {
                int usedSpace = 0;
                usedSpace += table.getNodeHeaderSpace();
                for (Index index : indices) {
                        usedSpace += 8;
                }
                for (Attribute attribute : searchKeys) {
                        usedSpace += attribute.getSize();
                }
                this.freeSpaceAmount = Catalog.getCatalog().getPageSize() - usedSpace;
        }

        private void update() {
                this.wasUpdated = true;
                calculateFreeSpace();
        }

        public int getFreeSpaceAmount() {
                return this.freeSpaceAmount;
        }

        public byte[] serializeNode() throws IOException, IllegalOperationException {
                int numKeys = searchKeys.size();
                int numIndices = indices.size();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);

                dos.writeInt(numKeys);
                dos.writeInt(numIndices);
                dos.writeBoolean(isLeaf);
                dos.writeInt(nextNode);
                dos.writeInt(parent);
                for (int i = 0; i < numKeys; i++) {
                        Attribute key = searchKeys.get(i);
                        key.serialize(dos);
                }
                for (int i = 0; i < numIndices; i++) {
                        dos.writeInt(indices.get(i).pageNumber);
                        dos.writeInt(indices.get(i).recordPointer);
                }
                System.out.println(getFreeSpaceAmount());
                for (int i = 0; i < getFreeSpaceAmount(); i++) {
                        dos.writeByte(0);
                }
                dos.flush();
                return baos.toByteArray();
        }

        public int getNodeNumber() {
                return nodeNumber;
        }

}