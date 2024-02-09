package storageManager;

import java.util.ArrayList;

import catalog.TableSchema;

public class Table {
    ArrayList<Page> pages = new ArrayList<Page>();
    TableSchema schema;

    // once init, will hold a table, the locations of its pages, and the format of each record
    public Table(ArrayList<GenericLocation> file_pages, TableSchema schema) {
        this.schema = schema;
        this.pages = getPages(file_pages);
    }
    
    public ArrayList<Page> getPages(ArrayList<GenericLocation> file_pages) {
        ArrayList<Page> pages = new ArrayList<Page>();

        for(int i=0; i < file_pages.size(); i++) {
            // not sure what determines the pageId, FOR NOW IT IS FILLER WITH i
            Page page = new Page(this.schema.getTableId(), i, 0);
            page.readPage(this.readPage((GenericLocation) file_pages.get(i)));
        }
        return pages;
    }

    // will return the given page at the given file
    public byte[] readPage(GenericLocation gl) {
        return null;
    }
}
