package hwp.sqlte;

public class UpdateOptions {

    private String table;
    private String columns;
    private boolean ignoreNullValues;

    public String table() {
        return table;
    }

    public UpdateOptions table(String table) {
        this.table = table;
        return this;
    }

    public String columns() {
        return columns;
    }

    public UpdateOptions columns(String columns) {
        this.columns = columns;
        return this;
    }

    public boolean isIgnoreNullValues() {
        return ignoreNullValues;
    }

    public UpdateOptions ignoreNullValues() {
        return setIgnoreNullValues(true);
    }

    public UpdateOptions setIgnoreNullValues(boolean b) {
        this.ignoreNullValues = b;
        return this;
    }

}
