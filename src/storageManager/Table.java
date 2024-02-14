package storageManager;

import java.util.ArrayList;

import catalog.Catalog;
import catalog.TableSchema;

public class Table {
    public TableSchema schema;
    private int numPages;
    //ArrayList<Page> pages = new ArrayList<Page>();


    // once init, will hold a table, the locations of its pages, and the format of each record
    public Table(TableSchema schema) {
        this.schema = schema;
        this.numPages = readNumPages();
    }

    // Moved this to buffer manager - Dan
    public ArrayList<Page> getPages(ArrayList<GenericLocation> file_pages) {
        ArrayList<Page> pages = new ArrayList<Page>();

        for(int i=0; i < file_pages.size(); i++) {
            // not sure what determines the pageId, FOR NOW IT IS FILLER WITH i
            Page page = new Page(this.schema.getTableId(), i, 0);
            //page.readPage(this.readPage((GenericLocation) file_pages.get(i)));
        }
        return pages;
    }

    // will return the given page at the given file
    public Page readPage(int pageNumber) {
        Page newPage = new Page(this.schema.getTableId(), pageNumber, Catalog.getCatalog().getPageSize());
        newPage.readPage();
        return null;
    }

    /**
     * Read from file the number of pages that belong to this table from memory
     * @return
     */
    private int readNumPages() {
        //String location = this.schema.getLocation; this method needs to exist
        return 0; // calculate size of file, divide by page size
    }

    /**
     * Get the number of pages that belong to this table
     * @return number of pages that belong to this table
     */
    public int getNumPages() {
        return numPages;
    }

    /**
     * Create a Page at the end of the file for this table
     * @return new page
     */
    public Page createPage() {
        int pageId = numPages;
        numPages++;
        return new Page(schema.getTableId(), pageId, Catalog.getCatalog().getPageSize());
    }

    /**
     * Increment the amount of pages this table has
     */
    public void incrementPageCount() {
        this.numPages++;
    }
}
