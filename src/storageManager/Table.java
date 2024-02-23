package storageManager;

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


    public Table(TableSchema schema) {
        this.schema = schema;
        this.schema.setNumPages(readNumPages());
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
            String location = getLocation();
            File file = new File(location);

            if (!file.exists()) {
                return 0;
            }
            long fileSize = file.length();
            int pageSize = Catalog.getCatalog().getPageSize();
            int numPages = (int) Math.ceil((double) fileSize / pageSize);

            return numPages;
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
        String location = getLocation();
        try (RandomAccessFile file = new RandomAccessFile(location, "rw");
             FileChannel fileChannel = file.getChannel()) {
                fileChannel.truncate((long) getNumPages() * Catalog.getCatalog().getPageSize());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writePage(Page page) {
        try {
            String location = getLocation();
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
            System.out.println(error.getMessage());
        }
    }


    public Page readPage(int pageNumber) {
        try {
            String location = getLocation();
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
            System.out.println(error.getMessage());
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
        if (!(isNull == 0 && attributeData.length == 1)) {
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
                    data = new String(attributeData);
                    break;
            }
        }

        return new Attribute(attributeSchema, data);
    }

    private String getLocation() {
        return Catalog.getCatalog().getLocation() + "/" + this.schema.getTableId() + ".bin";
    }
}