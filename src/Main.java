import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import Exceptions.InsufficientArgumentException;
import Exceptions.InvalidTypeException;
import catalog.Catalog;
import storageManager.StorageManager;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Main <dbLoc> <pageSize> <bufferSize>");
            System.exit(1);
        }
        
        String dbLoc = args[0];
        int pageSize = Integer.parseInt(args[1]);
        int bufferSize = Integer.parseInt(args[2]);
        
        System.out.println("Database Location: " + dbLoc);
        System.out.println("Page Size: " + pageSize);
        System.out.println("Buffer Size: " + bufferSize);

        // Initialize Catalog
        File dbDirectory = new File(dbLoc);
        Catalog catalog = Catalog.createCatalog(dbLoc, pageSize, bufferSize);
        if (dbDirectory.isDirectory()) {
            String[] files = dbDirectory.list();
            if (files != null && files.length > 0) {
                catalog.readBinary(dbLoc);
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
               
        while (true) {
            String input = scanner.nextLine();
            
            String[] commands = input.strip().split(" ");

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
                    
                    break;
                case "display":
                    dmlParser.displaySchema();
                    break;
                case "select":
                    dmlParser.select(commands[commands.length - 1].substring(0, commands[commands.length - 1].length() - 1));
                    break;
                
            }

            if (input.equalsIgnoreCase("quit")) {
                catalog.writeBinary(dbLoc);
                StorageManager.GetStorageManager().flushBuffer();
                break;
            } else if (input.equalsIgnoreCase("help")) {
                System.out.println(help());
            }
        }
        scanner.close();
    }

    public static void createTableParser(DDLParser ddlParser, Catalog catalog, String[] commands){
        String tableName = commands[2];
        String allConstraints = String.join(" ", Arrays.copyOfRange(commands, 3, commands.length - 1));
        String[] separatedConstraints = allConstraints.split(",");
        
        //clean up argument leading whitespaces
        for(int i = 0; i < separatedConstraints.length; i++){
            separatedConstraints[i] = separatedConstraints[i].strip();
        }

        try {
            ddlParser.createTable(catalog, tableName, new ArrayList<String>(Arrays.asList(separatedConstraints)));
        } catch (InvalidTypeException e) {
            // TODO Auto-generated catch block, replace with actual exception handling
            e.printStackTrace();
        }
    }

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
        }
    }

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

