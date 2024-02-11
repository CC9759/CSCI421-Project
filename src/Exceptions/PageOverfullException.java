package Exceptions;

public class PageOverfullException extends Exception{
    public PageOverfullException(int pageId) {
        super("Page " + pageId + " has overflowed");
    }
}
