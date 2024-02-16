import java.io.File;
import java.util.Scanner;

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
        while (true) {
            System.out.print("=^.^=> ");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("exit")) {
                catalog.writeBinary(dbLoc);
                StorageManager.GetStorageManager().flushBuffer();
                break;
            } else if (input.equalsIgnoreCase("help")) {
                System.out.println("help");
            }
        }
        
        scanner.close();
    }
}
