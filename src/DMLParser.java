import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Exceptions.DuplicateKeyException;
import Exceptions.NoTableException;
import Exceptions.PageOverfullException;
import WhereParser.Nodes.BoolOpNode;
import WhereParser.TokenParser.Parser;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;

import storageManager.Attribute;
import storageManager.StorageManager;
import storageManager.Record;

public class DMLParser {
    private StorageManager storageManager;

    public DMLParser(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    public ArrayList<Record> getAllRecords(TableSchema schema, String tableName) {
        ArrayList<Record> records;

        if (schema == null) { // no table by that name
            System.err.println("No table with name: " + tableName);
            return null;
        }
        
        try {
            records = this.storageManager.getAllRecords(schema.getTableId());
        } catch(NoTableException e) {
            System.err.println(e.getMessage());
            return null;
        }
        return records;
    }

    public void select(String tableName) {
        TableSchema schema = Catalog.getCatalog().getTableSchema(tableName);
        ArrayList<Record> records = getAllRecords(schema, tableName);

        if (records == null) 
            return;

        // print out attr names
        for (AttributeSchema attributeSchema : schema.getAttributeSchema()) {
            System.out.print(attributeSchema.getAttributeName() + " | ");
        }

        // print out the tuples
        for (Record record : records) {
            System.out.println("");
            ArrayList<Attribute> attrs = record.getAttributes();
            
            Collections.sort(attrs, new AttributeComparator());
            for (Attribute attr : attrs) {
                System.out.print(attr.getData() + "   ");
            }
        }
        System.out.println("");
    }

    public void delete(String tableName, String where) {
        TableSchema schema = Catalog.getCatalog().getTableSchema(tableName);
        ArrayList<Record> records = getAllRecords(schema, tableName);

        if (records == null) 
            return;

        // TODO : check for foreign key

        for (Record record : records) {
            try {
                BoolOpNode head = Parser.parseWhere(where);
                boolean pass = head.evaluate(record);
                if (pass) 
                    this.storageManager.deleteRecord(schema.getTableId(), record.getPrimaryKey());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // make sure the attr type aligns with the expected
    // double check that all doubles have a '.' in them
    public boolean confirmDataType(AttributeSchema attrSchema, String val) {
        var type = attrSchema.getAttributeType().type;
        
        if (type == AttributeType.TYPE.CHAR || type == AttributeType.TYPE.VARCHAR) {
            if (type == AttributeType.TYPE.CHAR && val.length() != attrSchema.getSize()) 
                return false;
            else if (type == AttributeType.TYPE.VARCHAR && val.length() > attrSchema.getSize())
                return false;
            return true;
        } else if (isInteger(val) && type == AttributeType.TYPE.INT) 
            return true;
        else if (isDouble(val) && type == AttributeType.TYPE.DOUBLE) 
            return true;
        else if (isBoolean(val) && type == AttributeType.TYPE.BOOLEAN) 
            return true;

        return false;
    }

    public boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isBoolean(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }

    public void update(String tableName, String column, String value, String where) {
        TableSchema schema = Catalog.getCatalog().getTableSchema(tableName);
        AttributeSchema updateAttr = null;

        // get attr name
        for (AttributeSchema attr : schema.getAttributeSchema()) {
            if (attr.getAttributeName().toLowerCase() == column) {
                updateAttr = attr;
            }
        }

        // making the value null violates constraint
        if (value == null && (updateAttr.isUnique() || updateAttr.isKey() || !updateAttr.isNull())) {
            return;
        }

        // column DNE
        if (updateAttr == null) {
            return;
        }

        // data typing wrong
        if (!confirmDataType(updateAttr, value)) {
            return;
        }

        ArrayList<Record> records = getAllRecords(schema, tableName);

        if (records == null) 
            return;

        for (Record record : records) {
            try {
                BoolOpNode head = Parser.parseWhere(where);
                boolean pass = head.evaluate(record);
                if (pass) {
                    if (updateAttr.getAttributeType().type == AttributeType.TYPE.CHAR || updateAttr.getAttributeType().type == AttributeType.TYPE.VARCHAR) 
                        record.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, value));
                    else if (updateAttr.getAttributeType().type == AttributeType.TYPE.INT) 
                        record.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, Integer.parseInt(value)));
                    else if (updateAttr.getAttributeType().type == AttributeType.TYPE.DOUBLE) 
                        record.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, Double.parseDouble(value)));
                    else if (updateAttr.getAttributeType().type == AttributeType.TYPE.BOOLEAN) 
                        record.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, Boolean.parseBoolean(value)));
                    this.storageManager.updateRecord(schema.getTableId(), record);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void displaySchema() {
        // displays the schema of the catalog, just prints out catalog's toString
        System.out.println(Catalog.getCatalog().toString());

        ArrayList<TableSchema> tables = Catalog.getCatalog().getTableSchema();

        System.out.println("");
        for (TableSchema table : tables) {
            displayInfo(table.getTableName());
        }
    }

    public void displayInfo (String tableName) {
        TableSchema schema = Catalog.getCatalog().getTableSchema(tableName);

        if (schema == null) { // no table by that name
            System.err.println("No table with name: " + tableName);
            return;
        }
        
        System.out.println("Table: " + schema.getTableName());

        System.out.print("Attributes: ");
        for (AttributeSchema attr : schema.getAttributeSchema()) {
            System.out.print(attr.getAttributeName() + ", ");
        }
        System.out.println();

        System.out.println("Number of Pages: " + schema.getNumPages());

        ArrayList<Record> records;
        try {
            records = this.storageManager.getAllRecords(schema.getTableId());
        } catch(NoTableException e) {
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("Number of Records: " + records.size());

    }

    public Boolean insert(ArrayList<Attribute> attributes, String name) {
        // first check that the recs align with table schema
        TableSchema schema = Catalog.getCatalog().getTableSchema(name);

        if (schema == null) { // no table by that name
            System.err.println("No table with name: " + name);
            return false;
        }

        ArrayList<AttributeSchema> attributeSchemas = schema.getAttributeSchema();

        if (attributes.size() != attributeSchemas.size()) { // different amount of attributes
            System.err.println("Expected " + attributeSchemas.size() + " attributes but got " + attributes.size() + ".");
            return false;
        }

        ArrayList<Attribute> legalAttributes = new ArrayList<Attribute>();
        // loop through attribute schema and ensure that inserted records are legal
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            AttributeSchema attributeSchema = attributeSchemas.get(i);

            // if value of data is null
            if (attribute.getData() == null) {
                if (attributeSchema.isUnique() || attributeSchema.isKey() || !attributeSchema.isNull()) { // value cannot be null
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " cannot be null." );
                    return false;
                }
                // value can be null
                legalAttributes.add(new Attribute(attributeSchema, null, attributeSchema.getAttributeType()));
                continue;
            }

            // assume all strings come in as CHAR
            if (attributeSchema.getAttributeType().type == AttributeType.TYPE.CHAR || attributeSchema.getAttributeType().type == AttributeType.TYPE.VARCHAR) {
                if (!(attribute.getData() instanceof String)) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a char/varchar type." );
                    return false;
                }

                if (((String) attribute.getData()).length() > attributeSchema.getSize()) { // char is too big
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " is a char/varchar type but is too large." );
                    return false;
                }

                // expected char of length N but got char length M
                if (attributeSchema.getAttributeType().type == AttributeType.TYPE.CHAR && attributeSchema.getSize() != ((String) attribute.getData()).length()) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " is a char of required length " + attributeSchema.getSize() + "." );
                    return false;
                }

                // replace with correct schema and data input
                try {
                    if (attributeSchema.getAttributeType().type == AttributeType.TYPE.VARCHAR) {
                        // if varChar, then create new attribute type with exact attr size
                        AttributeType varchar = new AttributeType(catalog.AttributeType.TYPE.VARCHAR, ((String)attribute.getData()).length());
                        legalAttributes.add(new Attribute(attributeSchema, (String) attribute.getData(), varchar));
                    } else {
                        legalAttributes.add(new Attribute(attributeSchema, (String) attribute.getData()));
                    }
                } catch (Error err) {
                    System.err.println("Wrong type.");
                    return false;
                }

            } else if (attributeSchema.getAttributeType().type == AttributeType.TYPE.INT || attributeSchema.getAttributeType().type == AttributeType.TYPE.DOUBLE) {
                // replace with correct schema and data input
                if (!(attribute.getData() instanceof Integer) && !(attribute.getData() instanceof Double)) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a int/double type.");
                    return false;
                }

                try {
                    if (attribute.getAttributeType().type == AttributeType.TYPE.INT)
                        legalAttributes.add(new Attribute(attributeSchema, (int) attribute.getData()));
                    else if (attribute.getAttributeType().type == AttributeType.TYPE.DOUBLE)
                        legalAttributes.add(new Attribute(attributeSchema, (double) attribute.getData()));
                    else {
                        System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a int/double type.");
                        return false;
                    }
                } catch (Error err) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a int/double type.");
                    return false;
                }

            } else if (attributeSchema.getAttributeType().type == AttributeType.TYPE.BOOLEAN) {
                if (!(attribute.getData() instanceof Boolean)) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a bool type.");
                    return false;
                }

                // replace with correct schema and data input
                try {
                    legalAttributes.add(new Attribute(attributeSchema, (boolean) attribute.getData()));
                } catch (Error err) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a bool type.");
                    return false;
                }
            }
        }

        // all the attributes are legal, and have a correct schema
        Record record = new Record(legalAttributes);
        try {
            this.storageManager.insertRecord(schema.getTableId(), record);
        } catch (PageOverfullException | NoTableException | DuplicateKeyException error) {
            System.err.println(error.getMessage());
            return false;
        }

        return true;
    }
}

class AttributeComparator implements Comparator<Attribute> {
    @Override
    public int compare(Attribute a1, Attribute a2) {
        return Integer.compare(a1.getAttributeId(), a2.getAttributeId());
    }
}
