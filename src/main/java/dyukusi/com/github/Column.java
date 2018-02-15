package dyukusi.com.github;

public class Column {
    private String name;
    private String typeName;

    Column(String columnName, String typeName) {
        this.name = columnName;
        this.typeName = typeName;
    }

    String getColumnName() {
        return this.name;
    }

    String getTypeName() {
        return this.typeName;
    }
}
