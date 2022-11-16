package hwp.sqlte;

public class RowMetadata {

    private int column;//基于 1
    private String label;//Column Label
    private String schema;// Schema Name
    private String table;//Table Name
    private int type;

    public RowMetadata(int column, String label) {
        this.column = column;
        this.label = label;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
