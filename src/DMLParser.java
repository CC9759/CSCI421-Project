import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import Exceptions.*;
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
        } catch (NoTableException e) {
            System.err.println(e.getMessage());
            return null;
        }
        return records;
    }

    public void select(ArrayList<String> selectArgs, ArrayList<String> fromArgs, String where, String orderByColumn) throws Exception {
        ArrayList<TableSchema> schemaList = new ArrayList<>();
        ArrayList<ArrayList<Record>> tableRecords = new ArrayList<>();

        // Retrieve copies of schemas and their records
        for (String tableName : fromArgs) {
            TableSchema schema = Catalog.getCatalog().getTableSchema(tableName);
            if (schema == null) {
                throw new NoTableException(tableName);
            }
            schema = schema.clone();
            schemaList.add(schema);
            tableRecords.add(getAllRecords(schema, tableName));
        }

        if (tableRecords.isEmpty())
            return;

        // Get records with attributes of format table.attributeName
        ArrayList<Record> records;
        if (tableRecords.size() == 1) {
            records = new ArrayList<>();
            for (Record record : tableRecords.get(0)) {
                Record recordClone = record.clone();
                for (Attribute attr : recordClone.getAttributes()) {
                    if (!attr.getAttributeName().contains(".")) {
                        String prefixedName = fromArgs.get(0) + "." + attr.getAttributeName();
                        recordClone.removeAttribute(attr.getAttributeName());
                        attr.setAttributeName(prefixedName);
                        recordClone.setAttribute(prefixedName, attr);
                    }
                }
                records.add(recordClone);
            }
        } else {
            records = crossProduct(tableRecords, fromArgs, 0, null);
        }

        boolean selectAll = false;

        if (selectArgs.get(0).equals("*") && selectArgs.size() == 1) {
            selectAll = true;
        } else if (selectArgs.contains("*")) {
            throw new IllegalOperationException("Invalid select");
        }

        // Set schema names to table.attributeName format. Determine presence of ambiguity in query
        for (TableSchema schema : schemaList) {
            for (AttributeSchema attributeSchema : schema.getAttributeSchema()) {
                if (!attributeSchema.getAttributeName().contains(".")) {
                    attributeSchema.setAttributeName(schema.getTableName() + "." + attributeSchema.getAttributeName());
                }
            }
        }
        HashMap<String, Integer> counts = new HashMap<>();
        ArrayList<AttributeSchema> selectAttributes = new ArrayList<>();
        if (selectAll) {
            for (TableSchema schema : schemaList) {
                Collections.sort(schema.getAttributeSchema(), new AttributeComparator());
                for (AttributeSchema attributeSchema : schema.getAttributeSchema()) {
                    selectAttributes.add(attributeSchema);
                    System.out.print(attributeSchema.getAttributeName() + " | ");
                }
            }
        } else {
            for (String selectArg : selectArgs) {
                AttributeSchema selectAttribute = null;
                String selectString = null;
                for (TableSchema schema : schemaList) {
                    if (selectArg.contains(".")) {
                        selectString = selectArg;
                    } else {
                        selectString = schema.getTableName() + "." + selectArg;
                    }
                    if (schema.getAttributeSchema(selectString) != null) {
                        selectAttribute = schema.getAttributeSchema(selectString);
                        if (!counts.containsKey(selectArg)) {
                            counts.put(selectArg, 0);
                        }
                        counts.put(selectArg, counts.get(selectArg) + 1);
                    }
                }
                if (!counts.containsKey(selectArg)) {
                    throw new Exception("No such attribute: " + selectArg);
                }
                int count = counts.get(selectArg);
                if (count > 1) {
                    throw new Exception(selectArg + " is ambiguous");
                } else if (count == 1) {
                    selectAttributes.add(selectAttribute);
                    System.out.print(selectAttribute.getAttributeName() + " | ");
                }
            }
        }

        if (orderByColumn != null && !orderByColumn.isEmpty()) {
            if (orderByColumn.contains(".")) {
                boolean columnExists = selectAttributes.stream()
                        .anyMatch(attr -> attr.getAttributeName().equals(orderByColumn));
                if (!columnExists) {
                    throw new Exception("OrderBy column " + orderByColumn + " not found");
                }
                records.sort((record1, record2) -> {
                    for (AttributeSchema attributeSchema : selectAttributes) {
                        if (attributeSchema.getAttributeName().equals(orderByColumn)) {
                            return record1.getAttribute(orderByColumn).compareTo(record2.getAttribute(orderByColumn));
                        }
                    }
                    return 0;
                });
            } else {
                List<AttributeSchema> matchingColumns = selectAttributes.stream()
                        .filter(attr -> attr.getAttributeName().endsWith(orderByColumn))
                        .collect(Collectors.toList());

                if (matchingColumns.isEmpty()) {
                    throw new Exception("OrderBy column " + orderByColumn + " not found");
                } else if (matchingColumns.size() != 1) {
                    throw new Exception("OrderBy column " + orderByColumn + " is ambiguous");
                }
                String matchingColumnName = matchingColumns.get(0).getAttributeName();
                records.sort((record1, record2) -> {
                    for (AttributeSchema attributeSchema : selectAttributes) {
                        if (attributeSchema.getAttributeName().endsWith(matchingColumnName)) {
                            String attrName = attributeSchema.getAttributeName();
                            return record1.getAttribute(attrName).compareTo(record2.getAttribute(attrName));
                        }
                    }
                    return 0;
                });
            }
        }

        // print out the tuples
        if (where != null) {
            BoolOpNode head = Parser.parseWhere(where);
            if (selectAll) {
                for (Record record : records) {
                    boolean pass = head.evaluate(record);
                    if (pass) {
                        System.out.println("");
                        for (TableSchema schema : schemaList) {
                            for (AttributeSchema attributeSchema : schema.getAttributeSchema()) {
                                var attr = record.getAttribute(attributeSchema.getAttributeName());
                                System.out.print(attr.getData() + "   ");
                            }
                        }
                    }
                }
                System.out.println("");
            } else {
                for (Record record : records) {
                    boolean pass = head.evaluate(record);
                    if (pass) {
                        System.out.println("");
                        for (AttributeSchema attributeSchema : selectAttributes) {
                            Attribute attr = record.getAttribute(attributeSchema.getAttributeName());
                            System.out.print(attr.getData() + "   ");
                        }
                    }
                }
                System.out.println("");
            }

        } else { // no where
            if (selectAll) {
                for (Record record : records) {
                    System.out.println("");
                    for (TableSchema schema : schemaList) {
                        for (AttributeSchema attributeSchema : schema.getAttributeSchema()) {
                            var attr = record.getAttribute(attributeSchema.getAttributeName());
                            System.out.print(attr.getData() + "   ");
                        }
                    }
                }
                System.out.println("");
            } else {
                for (Record record : records) {
                    System.out.println("");
                    for (AttributeSchema attributeSchema : selectAttributes) {
                        Attribute attr = record.getAttribute(attributeSchema.getAttributeName());
                        System.out.print(attr.getData() + "   ");
                    }
                }
                System.out.println("");
            }
        }
    }

    private ArrayList<Record> crossProduct(ArrayList<ArrayList<Record>> tableRecords, ArrayList<String> tableNames, int index, Record current) {
        ArrayList<Record> result = new ArrayList<>();

        if (index == tableRecords.size()) {
            if (current != null) {
                result.add(current);
            }
            return result;
        }

        for (Record rec : tableRecords.get(index)) {
            ArrayList<Attribute> combinedAttributes = new ArrayList<>();
            if (current != null) {
                combinedAttributes.addAll(current.getAttributes());
            }

            String tableName = tableNames.get(index);
            for (Attribute attribute : rec.getAttributes()) {
                Attribute attr = attribute.clone();
                if (!attr.getAttributeName().contains(".")) {
                    String prefixedName = tableName + "." + attr.getAttributeName();
                    AttributeSchema newSchema = new AttributeSchema(prefixedName, attr.getAttributeType(), attr.getAttributeId(), attr.isKey(), attr.isUnique(), attr.isNull());
                    Attribute newAttribute = new Attribute(newSchema, attr.getData());
                    combinedAttributes.add(newAttribute);
                } else {
                    combinedAttributes.add(attr);
                }
            }

            Record newRecord = new Record(combinedAttributes);
            result.addAll(crossProduct(tableRecords, tableNames, index + 1, newRecord));
        }

        return result;
    }

    public void delete(String tableName, String where) throws CloneNotSupportedException, NoTableException {
        TableSchema schema = formatSchemaName(tableName);

        ArrayList<Record> records = getAllRecords(schema, tableName);

        if (records == null) {
            return;
        }

        for (Record record : records) {
            try {
                if (where != null) {
                    BoolOpNode head = Parser.parseWhere(where);
                    if (!head.evaluate(record))
                        continue;
                }
                this.storageManager.deleteRecord(schema.getTableId(), record.getPrimaryKey());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    // make sure the attr type aligns with the expected
    // double check that all doubles have a '.' in them
    public boolean confirmDataType(AttributeSchema attrSchema, String val) {
        var type = attrSchema.getAttributeType().type;

        if ((type == AttributeType.TYPE.CHAR || type == AttributeType.TYPE.VARCHAR) && (val.charAt(0) == '\"' && val.charAt(val.length() - 1) == '\"')) {
            if (type == AttributeType.TYPE.CHAR && val.length() != attrSchema.getSize()) {
                System.out.println("Attribute (" + attrSchema.getAttributeName() + ") should be size " + attrSchema.getSize() + ".");
                return false;
            } else if (type == AttributeType.TYPE.VARCHAR && val.length() > attrSchema.getSize()) {
                System.out.println("Attribute (" + attrSchema.getAttributeName() + ") size should be less than or equal to " + attrSchema.getSize() + ".");
                return false;
            }
            return true;
        } else if (isInteger(val) && type == AttributeType.TYPE.INT)
            return true;
        else if (isDouble(val) && type == AttributeType.TYPE.DOUBLE)
            return true;
        else if (isBoolean(val) && type == AttributeType.TYPE.BOOLEAN)
            return true;

        System.out.println("Attribute (" + attrSchema.getAttributeName() + ") should be " + attrSchema.getAttributeType().type.toString() + ".");
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

    public void update(String tableName, String column, String value, String where) throws SyntaxErrorException, CloneNotSupportedException, NoTableException {
        // Set all columns to table.attrName
        TableSchema schema = formatSchemaName(tableName);
        AttributeSchema updateAttr = null;

        if (schema == null) { // no table by that name
            System.err.println("No table with name: " + tableName);
            return;
        }

        // get attr name

        for (AttributeSchema attr : schema.getAttributeSchema()) {
            if (attr.getAttributeName().toLowerCase().endsWith(column)) {
                updateAttr = attr;
            }
        }

        // column DNE
        if (updateAttr == null) {
            System.err.println("Attribute (" + column + ") DNE.");
            return;
        }

        // making the value null violates constraint
        if (value == null && (updateAttr.isUnique() || updateAttr.isKey() || !updateAttr.isNull())) {
            System.err.println("Attribute (" + column + ") has a null constraint.");
            return;
        }

        // data typing wrong
        if (value != null && !confirmDataType(updateAttr, value))
            return;

        ArrayList<Record> records = getAllRecords(schema, tableName);

        if (records == null) {
            return;
        }

        BoolOpNode head = null;
        if (where != null) {
            head = Parser.parseWhere(where);
        }
        for (Record record : records) {
            // Dont record saves on original record until actually inserted
            Record recordClone = record.clone();
            for (Attribute attribute : recordClone.getAttributes()) {
                if (!attribute.getAttributeName().contains(".")) {
                    String prefixedName = schema.getTableName() + "." + attribute.getAttributeName();
                    recordClone.removeAttribute(attribute.getAttributeName());
                    attribute.setAttributeName(prefixedName);
                    recordClone.setAttribute(prefixedName, attribute);
                }
            }

            try {
                if (head != null) {
                    if (!head.evaluate(recordClone))
                        continue;
                }
                if (value == null)
                    recordClone.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, value));
                else if (updateAttr.getAttributeType().type == AttributeType.TYPE.CHAR || updateAttr.getAttributeType().type == AttributeType.TYPE.VARCHAR)
                    recordClone.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, value));
                else if (updateAttr.getAttributeType().type == AttributeType.TYPE.INT)
                    recordClone.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, Integer.parseInt(value)));
                else if (updateAttr.getAttributeType().type == AttributeType.TYPE.DOUBLE)
                    recordClone.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, Double.parseDouble(value)));
                else if (updateAttr.getAttributeType().type == AttributeType.TYPE.BOOLEAN)
                    recordClone.setAttribute(updateAttr.getAttributeName(), new Attribute(updateAttr, Boolean.parseBoolean(value)));


                Record toDelete = this.storageManager.getRecordByPrimaryKey(schema.getTableId(), recordClone.getPrimaryKey());
                if (toDelete != record) {
                    // Records primary key was altered to be another records id.
                    if (toDelete != null) {
                        throw new DuplicateKeyException(toDelete.getPrimaryKey());
                    } else {
                        // Record is being set to a new primary key, delete old one
                        this.storageManager.deleteRecord(schema.getTableId(), record.getPrimaryKey());
                    }
                }

                this.storageManager.updateRecord(schema.getTableId(), recordClone);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private TableSchema formatSchemaName(String tableName) throws NoTableException, CloneNotSupportedException {
        TableSchema schema = Catalog.getCatalog().getTableSchema(tableName);
        if (schema == null) {
            throw new NoTableException(tableName);
        }
        schema = schema.clone();
        for (AttributeSchema attributeSchema : schema.getAttributeSchema()) {
            if (!attributeSchema.getAttributeName().contains(".")) {
                attributeSchema.setAttributeName(schema.getTableName() + "." + attributeSchema.getAttributeName());
            }
        }
        return schema;
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

    public void displayInfo(String tableName) {
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
        } catch (NoTableException e) {
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
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " cannot be null.");
                    return false;
                }
                // value can be null
                legalAttributes.add(new Attribute(attributeSchema, null, attributeSchema.getAttributeType()));
                continue;
            }

            // assume all strings come in as CHAR
            if (attributeSchema.getAttributeType().type == AttributeType.TYPE.CHAR || attributeSchema.getAttributeType().type == AttributeType.TYPE.VARCHAR) {
                if (!(attribute.getData() instanceof String)) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " should be a char/varchar type.");
                    return false;
                }

                if (((String) attribute.getData()).length() > attributeSchema.getSize()) { // char is too big
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " is a char/varchar type but is too large.");
                    return false;
                }

                // expected char of length N but got char length M
                if (attributeSchema.getAttributeType().type == AttributeType.TYPE.CHAR && attributeSchema.getSize() != ((String) attribute.getData()).length()) {
                    System.err.println("The attribute " + attributeSchema.getAttributeName() + " is a char of required length " + attributeSchema.getSize() + ".");
                    return false;
                }

                // replace with correct schema and data input
                try {
                    if (attributeSchema.getAttributeType().type == AttributeType.TYPE.VARCHAR) {
                        // if varChar, then create new attribute type with exact attr size
                        AttributeType varchar = new AttributeType(catalog.AttributeType.TYPE.VARCHAR, ((String) attribute.getData()).length());
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

class AttributeComparator implements Comparator<AttributeSchema> {
    @Override
    public int compare(AttributeSchema a1, AttributeSchema a2) {
        return CharSequence.compare(a1.getAttributeName(), a2.getAttributeName());
    }
}
