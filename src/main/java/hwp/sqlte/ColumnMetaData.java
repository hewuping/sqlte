package hwp.sqlte;

public class ColumnMetaData {

    private int column;//基于 1
    private String label;//Column Label (别名)
    private String name;//Column Name (实际列名)
    private String schema;// Schema Name
    private String table;//Table Name
    private int type;

    public ColumnMetaData(String schema, String table, int column) {
        this.schema = schema;
        this.table = table;
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public String getLabel() {
        return label;
    }


    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }


    public String getTable() {
        return table;
    }

    public int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "RowMetadata{" +
                "column=" + column +
                ", label='" + label + '\'' +
                ", schema='" + schema + '\'' +
                ", table='" + table + '\'' +
                ", type=" + type +
                '}';
    }
}
