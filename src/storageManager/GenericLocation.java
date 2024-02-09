package storageManager;

public class GenericLocation {
    
    int general_location; // file | page   | record
    int direct_location;  // page | record | bytes

    public GenericLocation(int general_location, int direct_location) {
        this.general_location = general_location;
        this.direct_location = direct_location;
    }
}
