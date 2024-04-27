package BPlusTree;

import Exceptions.IllegalOperationException;
import catalog.Catalog;
import storageManager.Attribute;
import storageManager.Table;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Collectors;

public class TreeNode {
    private List<Attribute> searchKeys;
    private List<Index> indices;
    public int nextNode;
    public int parent;
    public boolean isLeaf;
    public int nodeNumber;
    private int freeSpaceAmount;
    private final Table table;
    private boolean wasUpdated;

    // Node constructor
    public TreeNode(Table table, boolean isLeaf) {
        this.table = table;
        this.searchKeys = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.nextNode = -1;
        this.parent = -1;
        this.isLeaf = isLeaf;
        this.nodeNumber = table.getNumNodes();
        table.setNumNodes(table.getNumNodes() + 1);
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
     * Find the index of a record or the page it should be on
     * @param value primary key
     * @return index the record is on
     * @throws IllegalOperationException
     */
    public Index findIndex(Object value) throws IllegalOperationException {
        TreeNode foundNode = find(value, nodeNumber);
        int numKeys = foundNode.searchKeys.size();
        if (numKeys == 0) {
            return new Index(foundNode.nodeNumber, -1);
        }
        // Check if the primary key is on this page
        int left = 0;
        int right = numKeys - 1;
        while (left <= right) {
            int middle = left + (right - left) / 2;
            Object middleKey = foundNode.searchKeys.get(middle).getData();
            if (Attribute.compareTo(value, middleKey) == 0) {
                return foundNode.indices.get(middle); // Record found
            } else if (Attribute.compareTo(value, middleKey) < 0) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        return new Index(foundNode.nodeNumber, -1);
    }

    /**
     * function to insert a value in the B+ Tree
     *
     * @param value the specified value that will be insert
     * @return treenode which has the insert
     */
    public TreeNode insert(Attribute value, Index index) throws IllegalOperationException {
        TreeNode node = find(value.getData(), this.nodeNumber);

        if (node.contains(value.getData())) {
            return null;
        }

        if (node.searchKeys.size() + 1 == this.table.N) {

            insertToNode(node, value, index, false);
            divideNode(node);
        } else {
            insertToNode(node, value, index, false);
            node.writeNode();
        }
        return node;
    }

    private void insertToNode(TreeNode node, Attribute value, Index index, boolean isRight) {
        if (node.searchKeys.size() == 0) {
            node.addKey(value);
            if (node.isLeaf) {
                node.addIndex(index);
            }
            node.writeNode();
            return;
        } else if (node.searchKeys.size() == 1) {
            if (value.compareTo(node.searchKeys.get(0)) < 0) {
                node.addKey(0, value);
                node.addIndex(isRight ? 1 : 0, index);
            } else {
                node.addKey(value);
                node.addIndex(index);
            }

            node.writeNode();
            return;
        }
        // code below has potential to insert keys over max limit. dont write yet
        for (int i = 0; i < node.searchKeys.size(); i++) {
            if (value.compareTo(node.searchKeys.get(i)) < 0) {
                node.addKey(i, value);
                node.addIndex(isRight ? i + 1 : i, index);
                return;
            }
        }
        node.addKey(value); // if its the largest value in the node
        node.addIndex(index);
    }

    private void insertToNode(TreeNode node, Attribute value, boolean isRight) {
        if (node.searchKeys.size() == 0) {
            node.addKey(value);
            node.writeNode();
            return;
        } else if (node.searchKeys.size() == 1) {
            if (value.compareTo(node.searchKeys.get(0)) < 0) {
                node.addKey(0, value);
            } else {
                node.addKey(value);
            }

            node.writeNode();
            return;
        }
        // code below has potential to insert keys over max limit. dont write yet
        for (int i = 0; i < node.searchKeys.size(); i++) {
            if (value.compareTo(node.searchKeys.get(i)) < 0) {
                node.addKey(i, value);
                return;
            }
        }
        node.addKey(value); // if its the largest value in the node
    }

    private void divideNode(TreeNode node) throws IllegalOperationException {
        if (node.parent == -1) { // if im dividing the root
            TreeNode newLeftNode = new TreeNode(table, node.isLeaf);
            TreeNode newRightNode = new TreeNode(table, node.isLeaf);

            int lowestNodeSize = (int) Math.ceil(node.searchKeys.size() / 2.0);
            for (int i = 0; i < lowestNodeSize - 1; i++) {
                // copy values in newNode1
                newLeftNode.addKey(node.removeKey(0));
            }
            for (int i = 0; i < node.searchKeys.size(); i++) {
                // copy remaining values in newNode2
                newRightNode.addKey(node.searchKeys.get(i));
            }
            node.clearKeys();

            newLeftNode.parent = node.nodeNumber;
            newRightNode.parent = node.nodeNumber;
            if (node.isLeaf)
                newLeftNode.nextNode = newRightNode.nodeNumber;

            // get copy the pointers of node into new nodes
            int keysDivision = node.indices.size() / 2;
            for (int i = 0; i < keysDivision; i++) {
                // copy pointers in newNode1
                if (!node.isLeaf) {
                    TreeNode child = table.readNode(node.indices.get(0).pageNumber);
                    child.parent = newLeftNode.nodeNumber;
                    child.writeNode();
                }
                newLeftNode.addIndex(node.removeIndex(0));

            }
            for (int i = 0; i < node.indices.size(); i++) {
                // copy remaining keys in newNode2
                if (!node.isLeaf) {
                    TreeNode child = table.readNode(node.indices.get(i).pageNumber);
                    child.parent = newRightNode.nodeNumber;
                    child.writeNode();
                }

                newRightNode.addIndex(node.indices.get(i));

            }
            node.isLeaf = false;
            node.clearIndices();

            node.addIndex(new Index(newLeftNode.nodeNumber, -1));
            node.addIndex(new Index(newRightNode.nodeNumber, -1));
            node.addKey(newRightNode.searchKeys.get(0));
            if (!newRightNode.isLeaf) {
                newRightNode.removeKey(0);
            }
            newLeftNode.writeNode();
            newRightNode.writeNode();
            node.writeNode();

        } else { // split right, send to parent
            TreeNode newRightNode = new TreeNode(table, node.isLeaf);

            int lowestNodeSize = (int) Math.ceil(node.searchKeys.size() / 2.0);
            for (int i = 0; i < lowestNodeSize; i++) {
                // copy values in newNode so:
                newRightNode.addKey(node.removeKey(lowestNodeSize - 1));
            }

            newRightNode.parent = node.parent;

            newRightNode.nextNode = node.nextNode; // have new node to point to potential nextNode
            node.nextNode = newRightNode.nodeNumber; // have node point to newNode

            // copy the keys of node into newNode so: newNode = node[keyDiv:]
            int keysDivision = node.indices.size() / 2;
            for (int i = 0; i < lowestNodeSize; i++) {
                // copy keys in newNode
                if (!node.isLeaf) {
                    TreeNode child = table.readNode(node.indices.get(keysDivision).pageNumber);
                    child.parent = newRightNode.nodeNumber;
                    child.writeNode();
                }
                newRightNode.addIndex(node.removeIndex(keysDivision));
            }

            // put newNode in the correct position for parent keys
            TreeNode parent = table.readNode(node.parent);
            // insert the value to the parent node
            insertToNode(parent, newRightNode.searchKeys.get(0), new Index(newRightNode.nodeNumber, -1), true);
            if (!newRightNode.isLeaf) {
                newRightNode.removeKey(0);
                node.nextNode = -1;
                newRightNode.nextNode = -1;
            }
            node.writeNode();
            newRightNode.writeNode();

            if (parent.searchKeys.size() == this.table.N) {
                divideNode(parent);
            } else {
                parent.writeNode();
            }

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
                if (node.isLeaf) {
                    node.removeIndex(index);
                }
                node.writeNode();
            }
        }
        // if not root then delete and check for underfull
        else {
            TreeNode currNode = node;
            HashMap<Integer, Boolean> valueAlreadyRemoved = new HashMap<>();

            TreeNode parent = table.readNode(node.parent);
            int originalNodeIndex = parent.getKeyIndex(node);

            while (currNode != null) {
                int index = currNode.getValueIndex(value);
                valueAlreadyRemoved.put(currNode.nodeNumber, false);
                if (index > -1) {
                    currNode.removeKey(index);
                    valueAlreadyRemoved.put(currNode.nodeNumber, true);
                    if (currNode.isLeaf) {
                        currNode.removeIndex(index);
                    }
                    currNode.writeNode();
                }
                currNode = table.readNode(currNode.parent); /// doesnt check underfull until after
            }

            fixUnderfull(node, originalNodeIndex, valueAlreadyRemoved);
        }

        return true;
    }

    /**
     * Checks if the node has enough children/values and fixes it
     *
     * @param node the current node to check and fix for underfull
     */
    private void fixUnderfull(TreeNode node, int originalNodeIndex, HashMap<Integer, Boolean> valueAlreadyRemoved) throws IllegalOperationException {
        TreeNode currNode = node;
        while (currNode.parent != -1) {
            currNode = table.readNode(currNode.nodeNumber); // if parent was updated at all during last loop update it here
            TreeNode parent = table.readNode(currNode.parent);
            if (currNode.isUnderfull()) {
                int nodeIndex = parent.getKeyIndex(currNode);
                boolean borrowSucess = borrowNodes(currNode, nodeIndex, valueAlreadyRemoved);
                if (!borrowSucess) {
                    int mergedIntoId = mergeNodes(currNode, nodeIndex, valueAlreadyRemoved);
                    if (!currNode.isLeaf) {
                        TreeNode mergedInto = table.readNode(mergedIntoId);
                        for (int i = 0; i < mergedInto.searchKeys.size(); i++) {
                            TreeNode child = table.readNode(mergedInto.indices.get(i + 1).pageNumber);
                            mergedInto.searchKeys.set(i, child.searchKeys.get(0));

                        }
                        mergedInto.writeNode();
                    }

                }
            } else if (!currNode.isLeaf && currNode.isChildless()) {
                int nodeIndex = parent.getKeyIndex(currNode);
                int mergedIntoId = mergeNodes(currNode, nodeIndex, valueAlreadyRemoved);
                if (!currNode.isLeaf) {
                    TreeNode mergedInto = table.readNode(mergedIntoId);
                    for (int i = 0; i < mergedInto.searchKeys.size(); i++) {
                        TreeNode child = table.readNode(mergedInto.indices.get(i + 1).pageNumber);
                        mergedInto.searchKeys.set(i, child.searchKeys.get(0));

                    }
                    mergedInto.writeNode();
                }
            } else if (!currNode.isLeaf && currNode.searchKeys.size() < currNode.indices.size() - 1) {
                TreeNode parentToInsert = table.readNode(node.parent);
                TreeNode toInsert = table.readNode(parentToInsert.indices.get(originalNodeIndex).pageNumber); // jeez
                insertToNode(currNode, toInsert.searchKeys.get(0), false);
                currNode.writeNode();
            }
            currNode = parent;
        }
        currNode = table.readNode(currNode.nodeNumber);

        // if the root is underfull, then borrow from the leaf node
        if (currNode.searchKeys.size() < currNode.indices.size() - 1) {
            TreeNode parent = table.readNode(node.parent);
            TreeNode toInsert = table.readNode(parent.indices.get(originalNodeIndex).pageNumber);
            if (currNode.isLeaf) { // root is leaf
                insertToNode(currNode, toInsert.searchKeys.get(0), toInsert.indices.get(0), false);
            } else {
                insertToNode(currNode, toInsert.searchKeys.get(0), false);
            }
        }

        // if root and not enough children
        if (currNode.indices.size() < 2) {
            TreeNode onlyChild = table.readNode(currNode.indices.get(0).pageNumber);
            if (!onlyChild.isLeaf) {
                for (Index index : onlyChild.indices) {
                    TreeNode newChild = table.readNode(index.pageNumber);
                    newChild.parent = currNode.nodeNumber;
                    newChild.writeNode();
                }
            }

            currNode.clearIndices();
            currNode.addAllIndices(onlyChild.indices);
            currNode.clearKeys();
            currNode.addAllKeys(onlyChild.searchKeys);
            currNode.parent = -1;
            currNode.isLeaf = true;
        }
        currNode.writeNode();
    }

    /**
     * Tries to merge the current node to first the left node, then the right node
     *
     * @param node the current node to be merged
     * @return whether the operation is successful
     */
    private int mergeNodes(TreeNode node, int nodeIndex, HashMap<Integer, Boolean> valueAlreadyRemoved) throws IllegalOperationException {
        // merge with left sibling
        TreeNode parent = table.readNode(node.parent);
        TreeNode leftSibling = null, rightSibling = null;
        if (nodeIndex > 0) leftSibling = table.readNode(parent.indices.get(nodeIndex - 1).pageNumber);
        if (parent.indices.size() > 1 && nodeIndex < parent.indices.size() - 1) {
            rightSibling = table.readNode(parent.indices.get(nodeIndex + 1).pageNumber);
        }
        // when I do size checking it may leave the node unable to merge or borrow
        if (leftSibling != null && leftSibling.searchKeys.size() + node.searchKeys.size() < table.N) {
            leftSibling.addAllKeys(node.searchKeys);
            leftSibling.addAllIndices(node.indices);
            // inner node
            if (node.nextNode == -1) {
                // change children to point to left pointer
                if (!leftSibling.isLeaf) {
                    for (Index pointer : leftSibling.indices) {
                        TreeNode child = table.readNode(pointer.pageNumber);
                        child.parent = leftSibling.nodeNumber;
                        child.writeNode();
                    }
                }
            }
            // if leaf node, then we gotta change the nextNode value of the left sibling
            else if (node.isLeaf) {
                leftSibling.nextNode = node.nextNode;
            }
            leftSibling.writeNode();

            parent.removeIndex(nodeIndex);

            if (!valueAlreadyRemoved.get(parent.nodeNumber)) {
                parent.removeKey(nodeIndex - 1);
            }
            parent.writeNode();

            return leftSibling.nodeNumber;
        }
        // merge with right sibling
        else if (rightSibling != null && rightSibling.searchKeys.size() + node.searchKeys.size() < table.N) {

            int rightSiblingOriginalNum = parent.getValueIndex(rightSibling.searchKeys.get(0).getData());
            if (rightSiblingOriginalNum == -1) {
                int index = 0;
                var pks = parent.searchKeys;
                while (index < pks.size()) {
                    if (pks.get(index).compareTo(rightSibling.searchKeys.get(0)) < 0) {
                        index++;
                    } else {
                        break;
                    }
                }
                rightSiblingOriginalNum = index - 1;
            }
            node.addAllKeys(rightSibling.searchKeys);
            node.addAllIndices(rightSibling.indices);
            rightSibling.clearKeys();
            rightSibling.clearIndices();
            rightSibling.addAllKeys(node.searchKeys);
            rightSibling.addAllIndices(node.indices);
            // inner node
            if (node.nextNode == -1) {
                if (!node.isLeaf) {
                    // change children to point to right node
                    for (Index pointer : node.indices) {
                        TreeNode child = table.readNode(pointer.pageNumber);
                        child.parent = rightSibling.nodeNumber;
                        child.writeNode();
                    }
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


            rightSibling.writeNode();
            parent.removeIndex(nodeIndex);
            if (checkChildless(parent.indices.size(), table) && parent.parent != -1) { // UNLESS FOR ROOT FIXparent will be merged later. wont remove
                parent.searchKeys.set(rightSiblingOriginalNum, rightSibling.searchKeys.get(0));
            } else if (parent.indices.size() == parent.searchKeys.size() &&
                    parent.searchKeys.size() != 1 &&
                    parent.parent == -1) {
                parent.removeKey(rightSiblingOriginalNum);
            } else if (parent.parent != -1) {
                parent.removeKey(rightSiblingOriginalNum);
            }

            parent.writeNode();
            return rightSibling.nodeNumber;
        }

        return -1;
    }

    /**
     * Tries to borrow values from first left and then right sibling
     *
     * @param node      the current node that is borrowing
     * @param nodeIndex the index of the node
     * @return whether the operation is successful
     */
    private boolean borrowNodes(TreeNode node, int nodeIndex, HashMap<Integer, Boolean> valueAlreadyRemoved) throws IllegalOperationException {
        // try borrowing from left
        TreeNode parent = table.readNode(node.parent);
        TreeNode leftSibling = null;
        TreeNode rightSibling = null;
        if (nodeIndex != 0) {
            leftSibling = table.readNode(parent.indices.get(nodeIndex - 1).pageNumber);
        }
        if (nodeIndex != parent.indices.size() - 1) {
            rightSibling = table.readNode(parent.indices.get(nodeIndex + 1).pageNumber);
        }

        if (leftSibling != null && nodeIndex > 0 && leftSibling.searchKeys
                .size() > (Math.ceil((double) this.table.N / 2.0) - 1)) {
            Attribute borrowedValue = leftSibling.removeKey(leftSibling.searchKeys.size() - 1);
            Index borrowedIndex = leftSibling.removeIndex(leftSibling.indices.size() - 1);
            leftSibling.writeNode();
            insertToNode(node, borrowedValue, borrowedIndex, false);
            parent.searchKeys.set(nodeIndex - 1, borrowedValue);
            parent.writeNode();
            return true;
        }
        // try borrowing from right
        else if (rightSibling != null && parent.indices.size() > 1
                && nodeIndex < parent.indices.size() - 1
                && rightSibling.searchKeys
                .size() > (Math.ceil((double) this.table.N / 2.0) - 1)) {

            Attribute borrowedValue = rightSibling.removeKey(0);
            Index borrowedIndex = rightSibling.removeIndex(0);

            rightSibling.writeNode();
            insertToNode(node, borrowedValue, borrowedIndex, false);
            if (valueAlreadyRemoved.get(parent.nodeNumber)) {
                parent.searchKeys.set(nodeIndex - 1, rightSibling.searchKeys.get(0));
            } else {
                parent.searchKeys.set(nodeIndex, rightSibling.searchKeys.get(0));
            }

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
            return this.searchKeys.size() < Math.ceil(((double) this.table.N) / 2.0) - 1;
    }

    /**
     * checks if the node has enough children
     *
     * @return true or false if the node has enough children
     */
    private boolean isChildless() {
        return this.indices.size() < Math.floor((double) this.table.N / 2.0) + 1;
    }

    private static boolean checkChildless(int num, Table table) {
        return num < Math.floor((double) table.N / 2.0) + 1;
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
        for (int i = 0; i < getFreeSpaceAmount(); i++) {
            dos.writeByte(0);
        }
        dos.flush();
        return baos.toByteArray();
    }

    /**
     * Write a node to file
     */
    public void writeNode() {
        if (!this.wasUpdated) return;
        try {
            String location = table.schema.getNodeLocation();
            Catalog catalog = Catalog.getCatalog();
            RandomAccessFile file = new RandomAccessFile(location, "rw");
            long offset = getNodeNumber() * Catalog.getCatalog().getPageSize();
            byte[] nodeData = serializeNode();
            if (nodeData.length != catalog.getPageSize()) {
                throw new IllegalOperationException("Tried to write index page of size " + nodeData.length + " bytes which is not the defined page size");
            }
            file.seek(offset);
            file.write(nodeData);
            file.close();
        } catch (IOException | IllegalOperationException error) {
            System.err.println(error.getMessage());
        }
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void printTree() throws IllegalOperationException {
        Queue<Integer> queue = new LinkedList<Integer>();
        Queue<Integer> newLineQueue = new LinkedList<Integer>();
        TreeNode currentNode = this;
        int treeLevel = 0; // the current tree level
        // add all the leftmost nodes to get the new lines
        while (currentNode.isLeaf == false && currentNode.indices.size() != 0) {
            newLineQueue.add(currentNode.nodeNumber);
            currentNode = table.readNode(currentNode.indices.get(0).pageNumber);
            treeLevel += 1;
        }
        newLineQueue.add(currentNode.nodeNumber); // add the leaf node
        newLineQueue.poll(); // remove the root so there is no \n in before the root

        queue.add(this.nodeNumber);
        while (!queue.isEmpty()) { // later change to currentNode.isLeaf == false

            currentNode = table.readNode(queue.poll());
            for (int i = 0; i < treeLevel; i++) {
                System.out.print("\t");
            }
            currentNode.printValues();

            if (!currentNode.isLeaf) {
                for (Index index : currentNode.indices) {
                    queue.add(index.pageNumber);
                }

            }
            System.out.print(" (" + currentNode.nodeNumber + ", " + currentNode.parent + ")");

            if (queue.peek() == newLineQueue.peek()) {
                System.out.print("\n");
                newLineQueue.poll();
                treeLevel -= 1;
            } else {
                System.out.print("   ");
            }
        }
    }

    public void getAllLeaves(List<TreeNode> nodes) throws IllegalOperationException {
        Queue<Integer> queue = new LinkedList<Integer>();
        Queue<Integer> newLineQueue = new LinkedList<Integer>();
        TreeNode currentNode = this;
        // add all the leftmost nodes to get the new lines
        while (currentNode.isLeaf == false && currentNode.indices.size() != 0) {
            newLineQueue.add(currentNode.nodeNumber);
            currentNode = table.readNode(currentNode.indices.get(0).pageNumber);
        }
        newLineQueue.add(currentNode.nodeNumber); // add the leaf node
        newLineQueue.poll(); // remove the root so there is no \n in before the root

        queue.add(this.nodeNumber);
        while (!queue.isEmpty()) { // later change to currentNode.isLeaf == false

            currentNode = table.readNode(queue.poll());
            if (currentNode.isLeaf) {
                nodes.add(currentNode);
            }
            if (!currentNode.isLeaf) {
                for (Index index : currentNode.indices) {
                    queue.add(index.pageNumber);
                }

            }
            if (queue.peek() == newLineQueue.peek()) {
                newLineQueue.poll();
            }
        }
    }

    /**
     * function to print the values contained in a TreeNode
     */
    public void printValues() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(" | ",
                searchKeys.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList())));
        System.out.print(sb);
    }

    public List<Attribute> getSearchKeys() {
        return searchKeys;
    }

    public List<Index> getIndices() {
        return indices;
    }

}