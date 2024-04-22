/**
 * Table
 * Manages Table data and utility methods
 * @author Daniel Tregea
 */
package storageManager;

import BPlusTree.*;
import Exceptions.IllegalOperationException;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Table {
    public TableSchema schema;
    public int N;
    public int numNodes;


    public Table(TableSchema schema) {
        this.schema = schema;
        this.schema.setNumPages(readNumPages());

        AttributeSchema primaryKey = schema.getPrimaryKey();
        // - 17 for numNodes (4), numIndices(4), isLeaf(1), nextNode(4), parent(4)
        // 4 + 4 + (keySize) for each node info
//        System.out.println((Catalog.getCatalog().getPageSize() - getNodeHeaderSpace()));
//        System.out.println((8 + primaryKey.getSize()));
//        System.out.println((Catalog.getCatalog().getPageSize() - getNodeHeaderSpace()) /(8 + primaryKey.getSize()));
        this.N = Math.floorDiv((Catalog.getCatalog().getPageSize() - getNodeHeaderSpace()), (8 + primaryKey.getSize()));
        this.numNodes = readNumNodes();
        if (this.numNodes == 0) {
            TreeNode root = new TreeNode(this, 0, true);
            root.writeNode();
            this.numNodes = 1;
        }
    }


    public int getNumPages() {
        return this.schema.getNumPages();
    }


    /**
     * Get the number of pages that belong to this table
     *
     * @return number of pages that belong to this table
     */
    public int readNumPages() {
        try {
            String location = schema.getPageLocation();
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

    /**
     * Create a Page at the end of the file for this table
     *
     * @return new page
     */
    public Page createPage() {
        int pageId = this.getNumPages();
        updatePageCount(1);
        return new Page(schema.getTableId(), pageId, Catalog.getCatalog().getPageSize(), new ArrayList<>());
    }

    /**
     * Increment the amount of pages this table has
     */
    public void updatePageCount(int change) {
        this.schema.incrementNumPages(change);
        String location = schema.getPageLocation();
        try (RandomAccessFile file = new RandomAccessFile(location, "rw");
             FileChannel fileChannel = file.getChannel()) {
                fileChannel.truncate((long) getNumPages() * Catalog.getCatalog().getPageSize());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writePage(Page page) {
        try {
            String location = schema.getPageLocation();
            Catalog catalog = Catalog.getCatalog();
            RandomAccessFile file = new RandomAccessFile(location, "rw");
            long offset = page.getPageId() * Catalog.getCatalog().getPageSize();
            byte[] pageData = page.serializePage();
            if (pageData.length != catalog.getPageSize()) {
                throw new IllegalOperationException("Tried to write page of size " + pageData.length + " bytes which is not the defined page size");
            }
            file.seek(offset);
            file.write(pageData);
            file.close();
        } catch (IOException | IllegalOperationException error) {
            System.err.println(error.getMessage());
        }
    }


    public Page readPage(int pageNumber) {
        try {
            String location = schema.getPageLocation();
            RandomAccessFile file = new RandomAccessFile(location, "r");

            long pageOffset = pageNumber * Catalog.getCatalog().getPageSize();

            file.seek(pageOffset);

            // read page header
            int pageId = file.readInt();
            int numberOfSlots = file.readInt();
            int endOfFreeSpace = file.readInt();

            ArrayList<Record> records = new ArrayList<>();

            // read slot positions
            int[] recordPositions = new int[numberOfSlots];
            int[] recordSizes = new int[numberOfSlots];

            for (int i = 0; i < numberOfSlots; i++) {
                recordPositions[i] = file.readInt();
                recordSizes[i] = file.readInt();
            }

            // start reading each record
            for (int i = 0; i < numberOfSlots; i++) {
                file.seek(pageOffset + recordPositions[i]);
                byte[] recordBytes = new byte[recordSizes[i]];
                file.readFully(recordBytes);
                Record record = readRecord(recordBytes);
                records.add(record);
            }
            file.close();
            return new Page(this.schema.getTableId(), pageNumber, Catalog.getCatalog().getPageSize(), records);
        } catch (IOException error) {
            System.err.println(error.getMessage());
            return null;
        }
    }

    private Record readRecord(byte[] recordBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(recordBytes);
        DataInputStream dis = new DataInputStream(bais);

        int numAttributes = dis.readInt();

        ArrayList<Attribute> attributes = new ArrayList<>();
        int[] attributePositions = new int[numAttributes];
        int[] attributeSizes = new int[numAttributes];
        int[] attributeIds = new int[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            attributePositions[i] = dis.readInt();
            attributeSizes[i] = dis.readInt();
            attributeIds[i] = dis.readInt();
        }

        for (int i = 0; i < numAttributes; i++) {
            // seek to the attributes position
            dis.reset();
            dis.skipBytes(attributePositions[i]);

            byte[] attributeData = new byte[attributeSizes[i]];
            dis.readFully(attributeData);
            Attribute attribute = readAttribute(attributeData, this.schema.getAttributeSchema(attributeIds[i]));
            if (attribute != null) {
                attributes.add(attribute);
            }
        }

        return new Record(attributes);
    }

    private Attribute readAttribute(byte[] attributeData, AttributeSchema attributeSchema) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(attributeData);
        DataInputStream dis = new DataInputStream(bais);
        AttributeType type = attributeSchema.getAttributeType();
        Object data = null;
        dis.mark(1);
        byte isNull = dis.readByte();
        if (isNull == 0 && attributeData.length == 1) {
            // null bitmap is the same as a false boolean, if the attribute type is boolean, just read in a false
            if (type.type == AttributeType.TYPE.BOOLEAN) {
                data = Boolean.FALSE;
            }
        } else {
            dis.reset();
            switch (type.type) {
                case INT:
                    data = dis.readInt();
                    break;
                case DOUBLE:
                    data = dis.readDouble();
                    break;
                case BOOLEAN:
                    data = dis.readBoolean();
                    break;
                case CHAR:
                case VARCHAR:
                    data = (new String(attributeData).trim());
                    break;
            }
        }

        return new Attribute(attributeSchema, data);
    }

    public int getNodeHeaderSpace() {
        int headerSpace = 0;
        headerSpace += 4; // number of keys
        headerSpace += 4; // number of indices
        headerSpace += 1; // is leaf
        headerSpace += 4; // next pointer
        headerSpace += 4; // parent pointer
        return headerSpace;
    }

    public TreeNode readNode(int nodeNumber) throws IllegalOperationException {
        try {
            if (nodeNumber == -1) return null;
            String location = schema.getNodeLocation();
            RandomAccessFile file = new RandomAccessFile(location, "rw");
            long offset = nodeNumber * Catalog.getCatalog().getPageSize();
            file.seek(offset);
            AttributeSchema primaryKey = schema.getPrimaryKey();
            if (primaryKey == null) {
                throw new IllegalOperationException("Cant use indexes with no primary key defined");
            }
            int numberOfKeys = file.readInt();
            int numberOfIndices = file.readInt();
            boolean isLeaf = file.readBoolean();
            int nextNode = file.readInt();
            int parent = file.readInt();
            TreeNode newNode = new TreeNode(this, nodeNumber, isLeaf);
            newNode.nextNode = nextNode;
            newNode.parent = parent;

            int attributeSize = primaryKey.getSize();
            for (int i = 0; i < numberOfKeys; i++) {
                byte[] attributeData = new byte[attributeSize];
                file.readFully(attributeData);
                Attribute attribute = readAttribute(attributeData, primaryKey);
                newNode.addKey(attribute);
            }
            for (int i = 0; i < numberOfIndices; i++) {
                int pagePointer = file.readInt();
                int recordPointer = file.readInt();
                newNode.addIndex(new Index(pagePointer, recordPointer));
            }
            file.close();
            return newNode;
        } catch (IOException error) {
            System.err.println(error.getMessage());
        }
        return null;
    }

    public int readNumNodes() {
        try {
            String location = schema.getPageLocation();
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

    // Find the index of a primary key. If not found, find the page it should be inserted
    public Index findIndex(Object value) throws IllegalOperationException {
        TreeNode root = readNode(0);
        return root.findIndex(value);
    }

    // insert a primary key. prepropulate the index so use findIndex and where on the page it is
    public TreeNode insertNode(Attribute attribute, Index index) throws IllegalOperationException {
        TreeNode root = readNode(0);
        return root.insert(attribute, index);
    }

    // delete a primary key please and thank you
    public boolean deleteNode(Object value) throws IllegalOperationException {
        TreeNode root = readNode(0);
        return root.delete(value);
    }
}