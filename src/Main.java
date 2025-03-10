import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        if (args.length != 4) {
            System.err.println("Usage: java Main <dbLoc> <pageSize> <bufferSize> <indexing>");
            System.exit(1);
        }
        
        String dbLoc = args[0];
        int pageSize = 0;
        int bufferSize = 0;
        boolean indexing = false;
        try {
            pageSize = Integer.parseInt(args[1]);
            bufferSize = Integer.parseInt(args[2]);

            if (pageSize <= 0) {
                throw new Exception("Fatal: pageSize parameter must be a positive integer. Aborting.");
            }
            if (bufferSize <= 0) {
                throw new Exception("Fatal: bufferSize parameter must be a positive integer. Aborting.");
            }
            if (args[3].toLowerCase().equals("true")) {
                indexing = true;
            }
        } catch (NumberFormatException e) {
            System.err.println("Fatal: pageSize and/or bufferSize parameters contain no parseable integers. Aborting.");
            System.exit(1);
        } catch (Exception er) {
            System.err.println(er.getMessage());
            System.exit(1);
        }

        // Initialize Catalog
        Catalog catalog = null;
        try {
            Files.createDirectories(Paths.get(dbLoc));
            File dbDirectory = new File(dbLoc);
            catalog = Catalog.createCatalog(dbLoc, pageSize, bufferSize, indexing);
            String[] files = dbDirectory.list();
            if (files != null && files.length > 0) {
                Catalog.readBinary(dbLoc + "/catalog.bin");
                catalog = Catalog.getCatalog();
                catalog.setBufferSize(bufferSize);
                System.out.println("Initializing Database from existing file.");
            } else {
                System.out.println("No Database found in " + dbLoc + ". Creating new.");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Initialize Storage Manager
        StorageManager.InitStorageManager(bufferSize);

        if (!catalog.getIndexing() && indexing) {
            System.out.println("Turning on indexing");
            boolean success = StorageManager.GetStorageManager().turnOnIndexing();
            if (!success) {
                System.out.println("Failed to index. Aborting");
                System.exit(1);
            }
        }


        System.out.println("DB location: " + catalog.getLocation());
        System.out.println("Page size: " + catalog.getPageSize());
        System.out.println("Buffer size: " + catalog.getBufferSize());
        System.out.println("Indexing: " + catalog.getIndexing());
        
        Scanner scanner = new Scanner(System.in);
        //allows us to use non-static methods
        DDLParser ddlParser = new DDLParser();
        DMLParser dmlParser = new DMLParser(StorageManager.GetStorageManager());
        boolean running = true;
        while (running) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine();

                while(!input.endsWith(";")){
                    String nextLine = scanner.nextLine();
                    if(nextLine.strip().equals(";")){
                        input += nextLine;
                        break;
                    }
                    input += " " + nextLine;
                }

                //lowers everything outside of quotes
                input = toLowerCommand(input);
                // removes semicolon
                input = input.substring(0, input.length() - 1);
                String[] commands = input.strip().split(" ");
                
                switch(commands[0]){
                    default: System.out.println(help()); break;
                    case "create":
                        createTableParser(ddlParser, catalog, commands);
                        break;
                    case "drop":
                        ddlParser.dropTable(catalog, commands[commands.length - 1].substring(0, commands[commands.length - 1].length()));
                        break;
                    case "alter":
                        alterTableParser(ddlParser, catalog, commands);
                        break;
                    case "insert":
                        insertParser(dmlParser, input);
                        break;
                    case "display":
                        if(commands[1].equals("info") && commands.length > 2){
                            dmlParser.displayInfo(commands[commands.length - 1].substring(0, commands[commands.length - 1].length()));
                        }
                        else if(commands[1].equals("schema") && commands.length == 2){
                            dmlParser.displaySchema();
                        }
                        else{
                            System.out.println(help());
                        }
                        break;
                    case "select":
                        selectParser(dmlParser, commands);
                        break;
                    case "update":
                        updateParser(dmlParser, commands);
                        break;
                    case "delete":
                        deleteParser(dmlParser, commands);
                        break;
                    case "exit":
                    case "quit":
                        running = false;
                        catalog.writeBinary();
                        StorageManager.GetStorageManager().flushBuffer();
                        break;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
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
        try {
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
                if (colArgs.length < 2) {
                    throw new Exception("Columns must be denoted <name> <type>");
                }
                String name = colArgs[0];
                String type = colArgs[1];

                boolean primaryKey = column.contains("primarykey");
                boolean unique = column.contains("unique") || primaryKey;
                boolean notNull = column.contains("notnull") || primaryKey;

                newColumns.add(new Column(name, type, primaryKey, unique, notNull));
            }

            ddlParser.createTable(catalog, tableName, newColumns);
        } catch (Exception e) {
            System.err.println(e.getMessage());
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

        try {
            ddlParser.alterTable(catalog, tableName, allConstraints);
        } catch (InsufficientArgumentException | InvalidTypeException e) {
            System.err.println(e.getMessage());
        } catch (PageOverfullException | NoTableException | DuplicateKeyException | IllegalOperationException e) {
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
     * @param commandString the string of the command to process
     */
    public static void insertParser(DMLParser dmlParser, String commandString){
        commandString = commandString.strip();
        String[] commands = commandString.split("values");
        String[] commandFirstHalf = commands[0].split(" ");
        String tableName = commandFirstHalf[2];
        String allTuples = String.join(" ", Arrays.copyOfRange(commands, 1, commands.length)).strip();
        String[] separatedTuples = allTuples.split(",");

        for(String constraint : separatedTuples){
            try{
                boolean successfulOperation = dmlParser.insert(parseInsertValues(constraint, tableName), tableName);
                if(!successfulOperation){
                    System.out.println("Insert values failed");
                    return;
                }
            }  catch (ClassCastException error) {
                System.err.println("Insert values do not match schema types.");
                return;
            } catch(Exception e){
                System.err.println(e.getMessage());
                return;
            }
        }
    }

    /**
     * Takes in a single String representation of a tuple and converts each value
     * in the tuple to an appropriate Attribute
     * 
     * @param tupleString the string representation of a single tuple
     * @return an ArrayList of attributes derived from the tuple string
     * @throws IllegalOperationException 
     */
    public static ArrayList<Attribute> parseInsertValues(String tupleString, String tableName) throws IllegalOperationException{
        ArrayList<Attribute> attributes = new ArrayList<>();
        // removes special chars outside of numbers, characters, periods, and quotes
        tupleString = tupleString.replaceAll("[^0-9a-zA-Z.\\\" ]", "");
        
        // need some special regex for this cause quotation marks are a pain
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(tupleString);
        var tableSchemas = Catalog.getCatalog().getTableSchema(tableName);
        if (tableSchemas == null) {
            return null;
        }
        var attributeSchemas = tableSchemas.getAttributeSchema();
        int schemaPointer = 0, index_counter = 0;
        while (matcher.find()) {
            if(index_counter >= attributeSchemas.size()){
                throw new IllegalOperationException("Too many attributes");
            }
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
            index_counter += 1;
        }

        return attributes;
    }

    /**
     * argument parser for select command, takes in commands and separates the command
     * into 4 different arrays for select, from, where, and orderby args.
     * 
     * @param dmlParser the DMLParser instance
     * @param commands the string list of commands to process
     */
    public static void selectParser(DMLParser dmlParser, String[] commands) {
        ArrayList<String> commandsList = new ArrayList<String>(Arrays.asList(commands));
        ArrayList<String> fromArgs = new ArrayList<String>();
        String whereArgs = null;
        String orderbyColumn = null;
        int fromIndex = commandsList.indexOf("from");
        int whereIndex = commandsList.indexOf("where");
        int orderbyIndex = commandsList.indexOf("orderby");
        
        ArrayList<String> selectArgs = new ArrayList<String>(commandsList.subList(1, fromIndex));
        for (int i = 0; i < selectArgs.size(); i++) {
            String arg = selectArgs.get(i);
            long commaCount = arg.chars().filter(ch -> ch == ',').count();
            if (commaCount > 0) {
                selectArgs.set(i, arg.replace(",", ""));
            }
        }

        if(whereIndex != -1){
            fromArgs = new ArrayList<String>(commandsList.subList(fromIndex + 1, whereIndex));
            if(orderbyIndex != -1){
                whereArgs = String.join(" ", commandsList.subList(whereIndex + 1, orderbyIndex));
                orderbyColumn = commandsList.get(commandsList.size() - 1);
            }
            else{
                whereArgs = String.join(" ",commandsList.subList(whereIndex + 1, commandsList.size()));
            }
        }
        else if(orderbyIndex != -1){
            fromArgs = new ArrayList<String>(commandsList.subList(fromIndex + 1, orderbyIndex));
            orderbyColumn = commandsList.get(commandsList.size() - 1);
        }
        else{
            fromArgs = new ArrayList<String>(commandsList.subList(fromIndex + 1, commandsList.size()));
        }

        String fromList = String.join("", fromArgs);
        fromList = fromList.replaceAll(",", " ");
        fromArgs = new ArrayList<String>(Arrays.asList(fromList.split(" ")));

        for(String fromArg : fromArgs){
            if(fromArg.equals("") || fromArg.equals(" ")){
                fromArgs.remove(fromArg);
            }
        }

        try{
            dmlParser.select(selectArgs, fromArgs, whereArgs, orderbyColumn);
        } catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
    }

    /**
     * argument parser for update command, just manually takes in each part of the command
     * 
     * @param dmlParser the DMLParser instance
     * @param commands the string list of commands to process
     */
    public static void updateParser(DMLParser dmlParser, String[] commands) {
        ArrayList<String> commandsList = new ArrayList<String>(Arrays.asList(commands));
        int whereIndex = commandsList.indexOf("where");
        String tableName = commands[1];
        String columnName = commands[3];
        String value = commands[5];
        String whereString = null;

        if(value.equals("null")){
            value = null;
        }

        if(whereIndex != -1){
            whereString = String.join(" ", Arrays.copyOfRange(commands, whereIndex + 1, commands.length));
        }
        else{
            whereString = null;
        }
        
        try{
            dmlParser.update(tableName, columnName, value, whereString);
        } catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
    }

    /**
     * argument parser for delete command, just manually takes in each part of the command
     * 
     * @param dmlParser the DMLParser instance
     * @param commands the string list of commands to process
     */
    public static void deleteParser(DMLParser dmlParser, String[] commands){
        ArrayList<String> commandsList = new ArrayList<String>(Arrays.asList(commands));
        int whereIndex = commandsList.indexOf("where");
        String tableName = commands[2];
        String whereString = null;

        if(whereIndex != -1){
            whereString = String.join(" ", Arrays.copyOfRange(commands, whereIndex + 1, commands.length));
        }
        else{
            whereString = null;
        }
        
        try{
            dmlParser.delete(tableName, whereString);
        } catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
    }

    /**
     * lowers all chars in a given string except for the substrings wrapped in quotes
     * 
     * @param input the given string to lower chars by
     * @return the resulting toLowered string except for the substrings wrapped in quotes
     */
    public static String toLowerCommand(String input){
        StringBuilder result = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (currentChar == '"') {
                insideQuotes = !insideQuotes;
            }
            if (!insideQuotes) {
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
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

        helpMessage.append(
                "exit, quit: exit and save the database.\n" +
                        "\tUsage: exit; or quit;\n\n"
        );

        return helpMessage.toString();
    }
}

