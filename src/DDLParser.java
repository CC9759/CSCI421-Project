import java.util.ArrayList;
import java.util.Arrays;

import Exceptions.InvalidTypeException;
import catalog.Catalog;
import catalog.TableSchema;
import interfaces.AttributeSchema;
import interfaces.AttributeType;

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
                        var specialAttributes = Arrays.asList(Arrays.copyOfRange(attributes, 2, attributes.length));
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
        }

        /**
         * contacts the Catalog and Storage Manager
         * to alter a TableSchema and Table
         * 
         * @param tableName the name of the Table to alter
         * @param arguments the list of Table attributes to add/remove
         */
        public void alterTable(Catalog catalog, String tableName, ArrayList<String> arguments) {

        }
}