package BPlusTree;

public class Index {
    public int pageNumber;
    public int recordPointer;

    public Index(int pageNumber, int recordPointer) {
        this.pageNumber = pageNumber;
        this.recordPointer = recordPointer;
    }
}
