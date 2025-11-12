package models;

// Dùng để nạp dữ liệu cho ComboBox (Ma_X, Ten_X)
public class IdNamePair {
    private final long id;
    private final String name;
    
    public IdNamePair(long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public long getId() { return id; }
    public String getName() { return name; }
    
    @Override
    public String toString() {
        return name; 
    }
}