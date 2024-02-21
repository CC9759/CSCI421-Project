package DDLParser;

public class Column {
    private final String name;
    private final String type;
    private final boolean primaryKey;
    private final boolean unique;
    private final boolean notNull;
    private final String defaultValue;


    public Column(String name, String type, boolean primaryKey, boolean unique, boolean notNull, String defaultValue) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.unique = unique;
        this.notNull = notNull;
        this.defaultValue = defaultValue;
    }


    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isPrimaryKey() { return primaryKey; }
    public boolean isUnique() { return unique; }
    public boolean isNotNull() { return notNull; }
    public String getDefaultValue() { return defaultValue; }
}
