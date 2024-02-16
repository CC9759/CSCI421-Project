import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Exceptions.InsufficientArgumentException;
import Exceptions.InvalidTypeException;
import catalog.AttributeSchema;
import catalog.AttributeType;
import catalog.Catalog;
import catalog.TableSchema;

/**
 * Class for the DDLParser, will contain/have access
 * to a Catalog and/or Storage Manager instance
 */
public class DDLParser {
        /**
         * contacts the Catalog and Storage Manager
         * to add a TableSchema and Table
         * 
         * @param tableName the name of the Table to be added
         * @param arguments the list of Table attributes to add
         * @throws InvalidTypeException
         */
        public void createTable(Catalog catalog, String tableName, ArrayList<String> arguments)
                        throws InvalidTypeException {
                if (arguments == null) {
                        catalog.addTableSchema(new TableSchema(catalog.getTableSchemaLength(), tableName, null));
                        return;
                }

                ArrayList<AttributeSchema> attributesSchemas = new ArrayList<AttributeSchema>();
                arguments.replaceAll(e -> e.toUpperCase()); // make all uppercase for simplicity

                for (String arg : arguments) {
                        // split argument {name} {type} {keyType} {Key/!Key} {Unique} {NOT NULL}
                        String[] attributes = arg.split(" ");
                        String name = attributes[0];
                        AttributeType type = new AttributeType(attributes[1]);

                        List<String> specialAttributes = Arrays
                                        .asList(Arrays.copyOfRange(attributes, 2, attributes.length));
                        boolean key = specialAttributes.contains("KEY");
                        boolean unique = specialAttributes.contains("UNIQUE");
                        boolean nullable = !specialAttributes.contains("NOT NULL");

                        var schema = new AttributeSchema(name, type, key, unique, nullable);
                        attributesSchemas.add(schema);
                }
                catalog.addTableSchema(new TableSchema(catalog.getTableSchemaLength(), tableName, attributesSchemas));
        }

        /**
         * contacts the Catalog and Storage Manager
         * to drop/remove a TableSchema and Table
         * 
         * @param tableName the name of the Table to drop
         */
        public void dropTable(Catalog catalog, String tableName) {
                catalog.removeTableSchema(tableName);
                // NOTE: should update Catalog file for changes to save
        }

        /**
         * contacts the Catalog and Storage Manager
         * to alter a TableSchema and Table
         * 
         * @param tableName the name of the Table to alter
         * @param arguments the list of Table attributes to add/remove
         */
        public void alterTable(Catalog catalog, String tableName, ArrayList<String> arguments)
                        throws InsufficientArgumentException {
                String keyWord = arguments.get(0).toUpperCase();
                switch (keyWord) {
                        case "ADD":
                                if (arguments.size() != 3)
                                        throw new InsufficientArgumentException(keyWord);
                                var newAttributes = new AttributeSchema(keyWord, null, false, false, false);
                                // catalog.getTableSchema(tableName).addAttributeSchema();
                                // String defaultValue = "null";
                                // if (specialAttributes.contains("DEFAULT")) {
                                // defaultValue = specialAttributes.get(specialAttributes.indexOf("DEFAULT") +
                                // 1);
                                // }
                                // TODO
                                break;
                        case "DROP":
                                // TODO
                                break;
                        default:
                                break;
                }
        }
}