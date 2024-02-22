import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import DDLParser.DDLParser;
import DDLParser.Column;
import Exceptions.*;
import catalog.Catalog;
import storageManager.StorageManager;
import storageManager.Attribute;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Main <dbLoc> <pageSize> <bufferSize>");
            System.exit(1);
        }
        
        String dbLoc = args[0];
        int pageSize = Integer.parseInt(args[1]);
        int bufferSize = Integer.parseInt(args[2]);


        // Initialize Catalog
        File dbDirectory = new File(dbLoc);
        Catalog catalog = Catalog.createCatalog(dbLoc, pageSize, bufferSize);
        if (dbDirectory.isDirectory()) {
            String[] files = dbDirectory.list();
            if (files != null && files.length > 0) {
                Catalog.readBinary(dbLoc + "/catalog.bin");
                catalog = Catalog.getCatalog();
                System.out.println("Initializing Database from existing file.");
            } else {
                System.out.println("No Database found in " + dbLoc + ". Creating new.");
            }
        } else {
            System.out.println("Path \'" + dbLoc + "\" provided is not a valid directory. Aborting.");
            System.exit(1);
        }

        // Initialize Storage Manager
        StorageManager.InitStorageManager(bufferSize);
        
        Scanner scanner = new Scanner(System.in);
        //allows us to use non-static methods
        DDLParser ddlParser = new DDLParser();
        DMLParser dmlParser = new DMLParser(StorageManager.GetStorageManager(), catalog);
        boolean running = true;
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine();
            
            while(!input.endsWith(";")){
                input += "" + scanner.nextLine();
            }

            String[] commands = input.strip().split(" ");
            System.out.println(Arrays.toString(commands));
            switch(commands[0].toLowerCase()){
                default: System.out.println(help()); break;
                case "create":
                    createTableParser(ddlParser, catalog, commands);
                    break;
                case "drop":
                    ddlParser.dropTable(catalog, commands[commands.length - 1].substring(0, commands[commands.length - 1].length() - 1));
                    break;
                case "alter":
                    alterTableParser(ddlParser, catalog, commands);
                    break;
                case "insert":
                    insertParser(dmlParser, commands);
                    break;
                case "display":
                    dmlParser.displaySchema();
                    break;
                case "select":
                    dmlParser.select(commands[commands.length - 1].substring(0, commands[commands.length - 1].length() - 1));
                    break;
                case "exit;":
                case "quit;":
                    running = false;
                    catalog.writeBinary();
                    StorageManager.GetStorageManager().flushBuffer();
                    break;
            }
        }
        scanner.close();
    }

    /**
     * argument parser for create Table command, takes in the commands and calls DDLParser.DDLParser to handle
     * processed commands
     * 
     * @param ddlParser the DDLParser.DDLParser instance
     * @param catalog the catalog we are editing to
     * @param commands the string list of commands to process
     */
    public static void createTableParser(DDLParser ddlParser, Catalog catalog, String[] commands) {

        String tableName = commands[2];
        // Remove left parenthesis if user attached it to the table name
        if (tableName.contains("(")) {
            tableName = tableName.substring(0, tableName.indexOf("("));
        }

        // get the original command to parse all columns
        String command = String.join(" ", Arrays.asList(commands));
        int startIndex = command.indexOf("(") + 1;
        int endIndex = command.lastIndexOf(")");
        String allColumnsString = command.substring(startIndex, endIndex);
        String[] columnParams = allColumnsString.split(",");

        ArrayList<Column> newColumns = new ArrayList<>();

        for (String column : columnParams) {
            column = column.trim();

            String[] colArgs = column.split("\\s+", 3);
            String name = colArgs[0];
            String type = colArgs[1];

            boolean primaryKey = column.toLowerCase().contains("primarykey");
            boolean unique = column.toLowerCase().contains("unique") || primaryKey;
            boolean notNull = column.toLowerCase().contains("notnull") || primaryKey;

            newColumns.add(new Column(name, type, primaryKey, unique, notNull));
        }

        try {
            ddlParser.createTable(catalog, tableName, newColumns);
        } catch (InvalidTypeException e) {
            e.printStackTrace();
        }
    }

    /**
     * argument parser for alter table command, takes in the commands and calls DDLParser.DDLParser to handle
     * processed commands
     * 
     * @param ddlParser the DDLParser.DDLParser instance
     * @param catalog the catalog we are editing to
     * @param commands the string list of commands to process
     */
    public static void alterTableParser(DDLParser ddlParser, Catalog catalog, String[] commands){
        String tableName = commands[2];
        String allConstraints = String.join(" ", Arrays.copyOfRange(commands, 3, commands.length));
        // removes the semicolon
        allConstraints = allConstraints.substring(0, allConstraints.length() - 1);
        
        try {
            ddlParser.alterTable(catalog, tableName, allConstraints);
        } catch (InsufficientArgumentException e) {
            // TODO Auto-generated catch block, replace with actual exception handling
            e.printStackTrace();
        } catch (InvalidTypeException e) {
            // TODO Auto-generated catch block, replace with actual exception handling
            e.printStackTrace();
        } catch (PageOverfullException | NoTableException | DuplicateKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * argument parser for insert into command, takes in each tuple in the commands
     * and converts values in the tuples to a list of attributes. Individually inserts
     * and processes each tuple according to the writeup. calls the DMLParser to handle
     * catalog editing.
     * 
     * @param dmlParser the DMLParser instance
     * @param commands the string list of commands to process
     */
    public static void insertParser(DMLParser dmlParser, String[] commands){
        ArrayList<ArrayList<Attribute>> allAttributes = new ArrayList<>();
        String tableName = commands[2];
        String allTuples = String.join(" ", Arrays.copyOfRange(commands, 4, commands.length));
        String[] separatedTuples = allTuples.split(",");

        for(String constraint : separatedTuples){
            dmlParser.insert(parseInsertValues(constraint, tableName), tableName);
        }
    }

    /**
     * Takes in a single String representation of a tuple and converts each value
     * in the tuple to an appropriate Attribute
     * 
     * @param tupleString the string representation of a single tuple
     * @return an ArrayList of attributes derived from the tuple string
     */
    public static ArrayList<Attribute> parseInsertValues(String tupleString, String tableName){
        ArrayList<Attribute> attributes = new ArrayList<>();
        // removes special chars outside of numbers, characters, periods, and quotes
        tupleString = tupleString.replaceAll("[^0-9a-zA-Z.\\\" ]", "");
        
        // need some special regex for this cause quotation marks are a pain
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(tupleString);
        var tableSchemas = Catalog.getCatalog().getTableSchema(tableName);
        if (tableSchemas == null) {
            System.out.println("Table" + tableSchemas + " DNE.");
            return null;
        }
        var attributeSchemas = tableSchemas.getAttributeSchema();
        int schemaPointer = 0;
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                attributes.add(new Attribute(attributeSchemas.get(schemaPointer++), matcher.group(1)));
            } else {
                String match = matcher.group(2);
                if (match.matches("-?\\d+")) {
                    attributes.add(new Attribute(attributeSchemas.get(schemaPointer++), Integer.parseInt(match)));
                } else if (match.matches("-?\\d+\\.\\d+")) {
                    attributes.add(new Attribute(attributeSchemas.get(schemaPointer++), Double.parseDouble(match)));
                } else if (match.equals("true") || match.equals("false")) {
                    attributes.add(new Attribute(attributeSchemas.get(schemaPointer++), Boolean.parseBoolean(match)));
                } else if (match.equals("null")){
                    attributes.add(new Attribute(attributeSchemas.get(schemaPointer++), null));
                }
                // if it doesn't match anything at all then just add the attribute as a string
                else {
                    attributes.add(new Attribute(attributeSchemas.get(schemaPointer++), match));
                }
            }
        }

        return attributes;
    }

    /**
     * function for the help message
     * 
     * @return string representation of the help message
     */
    public static String help(){
        StringBuilder helpMessage =  new StringBuilder();
        helpMessage.append(
            "create table: used to create a table\n" +
            "\tUsage:\n" +
            "\t\tcreate table <name>(\n" +
            "\t\t\t<a_name> <a_type> <constraint_1>,\n" +
            "\t\t\t<constraint>\n" + 
            "\t\t);\n\n");

        helpMessage.append(
            "drop table: used to drop a table from the database; including its data.\n" +
            "\tUsage: drop table <name>;\n\n"
        );

        helpMessage.append(
            "alter table: used to add/remove columns from a table.\n" +
            "\tUsage:\n" +
            "\t\talter table <name> drop <a_name>;\n" +
            "\t\talter table <name> add <a_name> <a_type>;\n" +
            "\t\talter table <name> add <a_name> <a_type> default <value>;\n\n");

        helpMessage.append(
            "insert: used to insert data into a table.\n" +
            "\tUsage: insert into <name> values <tuples>;\n\n"
        );

        helpMessage.append(
            "select: used to access data in tables.\n" +
            "\tUsage:\n" +
            "\t\tselect *\n" +
            "\t\tfrom <name>;\n\n"
        );

        helpMessage.append(
            "display schema: used to display the catalog of the database in an easy to read format.\n" +
            "\tUsage: display schema;\n\n"
        );

        helpMessage.append(
            "display info: used to display the information about a table in an easy to read format.\n" +
            "\tUsage: display info <name>;\n\n"
        );

        return helpMessage.toString();
    }
}

