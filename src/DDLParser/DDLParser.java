package DDLParser;

import java.util.*;

import Exceptions.*;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;
import storageManager.Attribute;
import storageManager.Record;
import storageManager.StorageManager;

/**
 * Class for the DDLParser.DDLParser, will contain/have access
 * to a Catalog and/or Storage Manager instance
 */
public class DDLParser {
    /**
     * contacts the Catalog and Storage Manager
     * to add a TableSchema and Table
     *
     * @param tableName the name of the Table to be added
     * @param columns the list of Table attributes to add in the form:
     *                  {column1 datatype,
     *                  column2 datatype,
     *                  column3 datatype,...}
     * @throws InvalidTypeException
     */
    public void createTable(Catalog catalog, String tableName, ArrayList<Column> columns)
            throws InvalidTypeException, Exception {
        ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>();
        for (TableSchema tableSchema: catalog.getTableSchema()) {
            if (tableSchema.getTableName().equalsIgnoreCase(tableName)) {
                throw new Exception("Can not create two tables of the same name");
            }
        }
        if (columns.size() == 0) {
            throw new Exception("Cannot create a table with zero attributes");
        }
        int numPrimaryKeys = 0;
        HashSet<String> attributeNames = new HashSet<>();
        for (Column column : columns) {
            AttributeType type = new AttributeType(column.getType());
            boolean key = column.isPrimaryKey();
            boolean unique = column.isUnique();
            boolean nullable = !column.isNotNull();
            if (key) {
                numPrimaryKeys++;
            }

            AttributeSchema schema = new AttributeSchema(column.getName(), type, attributeSchemas.size(), key, unique, nullable);
            if (attributeNames.contains(column.getName())) {
                throw new Exception("Can not have two attributes with the same name");
            } else {
                attributeNames.add(column.getName());
            }
            attributeSchemas.add(schema);
        }
        if (numPrimaryKeys != 1) {
            throw new Exception("Tried to create a table with the incorrect number of primary keys. Required: 1, Given: " + numPrimaryKeys);
        }
        catalog.addTableSchema(new TableSchema(catalog.getTableSchemaLength(), tableName, attributeSchemas));
    }

    /**
     * contacts the Catalog and Storage Manager
     * to drop/remove a TableSchema and Table
     *
     * @param tableName the name of the Table to drop
     */
    public void dropTable(Catalog catalog, String tableName) throws NoTableException {
        deleteAllRecords(tableName);
        catalog.removeTableSchema(tableName);
    }

    /**
     * contacts the Catalog and Storage Manager
     * to alter a TableSchema and Table
     * @param catalog
     * @param tableName the name of the Table to alter
     * @param argument the Table attributes to add/remove in the form:
     *                  ADD column_name datatype (DEFAULT value)
     *                  DROP COLUMN column_name
     * @throws InvalidTypeException
     */
    public void alterTable(Catalog catalog, String tableName, String argument)
            throws InsufficientArgumentException, InvalidTypeException, PageOverfullException, NoTableException, DuplicateKeyException {
        String[] attributes = argument.split(" ");
        String keyWord = attributes[0].toUpperCase();
        TableSchema tableSchema = catalog.getTableSchema(tableName);
        if (tableSchema == null) {
            throw new NoTableException(tableName);
        }
        int numExistingAttributes = catalog.getTableSchema(tableName).getAttributeSchema().size();
        switch (keyWord) {
            case "ADD":
                if (!(attributes.length == 3 || attributes.length == 5))
                    throw new InsufficientArgumentException(keyWord);

                // get a list of the instructions
                List<String> instruc = Arrays
                        .asList(Arrays.copyOfRange(attributes, 1, attributes.length));

                String defaultValue = "null";
                if (instruc.contains("DEFAULT")) {
                    defaultValue = instruc.get(instruc.indexOf("DEFAULT") + 1);
                }
                var newAttributes = new AttributeSchema(instruc.get(0),
                        new AttributeType(instruc.get(1)), numExistingAttributes, false, false, true, defaultValue);

                // look for a attribute that shares the name and replace it
                if (tableSchema.getAttributeSchema(instruc.get(0)) != null) {
                    tableSchema.removeAttributeSchema(instruc.get(0));
                }
                tableSchema.addAttributeSchema(newAttributes);
                updateAttributes(tableName, newAttributes, "add");
                break;
            case "DROP":
                if (tableSchema.getAttributeSchema(attributes[1]) != null) {
                    updateAttributes(tableName, tableSchema.getAttributeSchema(attributes[1]), "remove");
                    tableSchema.removeAttributeSchema(attributes[1]);
                }
                break;
            default:
                break;
        }
    }

    private void updateAttributes(String tableName, AttributeSchema attributeSchema, String action) throws NoTableException, PageOverfullException, DuplicateKeyException {
        var storageManager = StorageManager.GetStorageManager();
        var tableId = Catalog.getCatalog().getTableSchema(tableName).getTableId();
        var allRecords = storageManager.getAllRecords(tableId);

        for (Record record : allRecords) {
            if (action.equalsIgnoreCase("add")) {
                var newAttribute = new Attribute(attributeSchema, stringToType(attributeSchema.defaultValue(), attributeSchema.getAttributeType()));
                record.setAttribute(newAttribute.getAttributeName(), newAttribute);
            } else if (action.equalsIgnoreCase("remove")) {
                record.removeAttribute(attributeSchema.getAttributeName());
            }

            storageManager.updateRecord(tableId, record);

        }
    }

    private void deleteAllRecords(String tableName) throws NoTableException {
        var storageManager = StorageManager.GetStorageManager();
        var tableId = Catalog.getCatalog().getTableSchema(tableName).getTableId();
        var allRecords = storageManager.getAllRecords(tableId);

        for (Record record : allRecords) {
            storageManager.deleteRecord(tableId, record.getPrimaryKey());
        }
    }

    private Object stringToType(String str, AttributeType type) {
        if (Objects.equals(str, "null")) {
            return null;
        }
        switch (type.type) {
            case INT:
                return Integer.parseInt(str);
            case DOUBLE:
                return Double.parseDouble(str);
            case BOOLEAN:
                return Boolean.parseBoolean(str);
            case CHAR:
            case VARCHAR:
                return str;
        }
        return null;
    }
}