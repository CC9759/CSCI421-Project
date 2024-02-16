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
         * @param arguments the list of Table attributes to add in the form:
         *                  {column1 datatype,
         *                  column2 datatype,
         *                  column3 datatype,...}
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
                        boolean nullable = !specialAttributes.contains("NOT NULL"); // TODO: test this

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
         * @param arguments the Table attributes to add/remove in the form:
         *                  ADD column_name datatype (DEFAULT value)
         *                  DROP COLUMN column_name
         * @throws InvalidTypeException
         */
        public void alterTable(Catalog catalog, String tableName, String argument)
                        throws InsufficientArgumentException, InvalidTypeException {
                String[] attributes = argument.toUpperCase().split(" ");
                String keyWord = attributes[0];
                switch (keyWord) {
                        case "ADD":
                                if (attributes.length != 3 || attributes.length != 5)
                                        throw new InsufficientArgumentException(keyWord);

                                // get a list of the instructions
                                List<String> instruc = Arrays
                                                .asList(Arrays.copyOfRange(attributes, 1, attributes.length));

                                String defaultValue = "null";
                                if (instruc.contains("DEFAULT")) {
                                        defaultValue = instruc.get(instruc.indexOf("DEFAULT") + 1);
                                }
                                var newAttributes = new AttributeSchema(instruc.get(0),
                                                new AttributeType(instruc.get(1)), false, false, false, defaultValue);

                                // look for a attribute that shares the name and replace it
                                if (catalog.getTableSchema(tableName).getAttributeSchema(instruc.get(0)) != null) {
                                        catalog.getTableSchema(tableName).removeAttributeSchema(instruc.get(0));
                                }
                                catalog.getTableSchema(tableName).addAttributeSchema(newAttributes);
                                break;
                        case "DROP":
                                if (catalog.getTableSchema(tableName).getAttributeSchema(attributes[2]) != null) {
                                        catalog.getTableSchema(tableName).removeAttributeSchema(attributes[2]);
                                }
                                break;
                        default:
                                break;
                }
        }
}