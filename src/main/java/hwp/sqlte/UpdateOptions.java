package hwp.sqlte;

import hwp.sqlte.util.ObjectUtils;
import hwp.sqlte.util.StringUtils;

import java.util.Objects;

/**
 * @since 0.3.0
 */
public class UpdateOptions {

    public static final UpdateOptions DEFAULT = new UpdateOptions();

    private String table;
    private String[] columns;
    private boolean ignoreNullValues;
    private int batchSize = 1000;

    private SqlHandler sqlHandler = SqlHandler.DEFAUTL;

    private GeneratedKeysConsumer generatedKeysConsumer;

    public static UpdateOptions of() {
        return new UpdateOptions();
    }

    public static UpdateOptions ofTable(String table) {
        return new UpdateOptions().setTable(table);
    }

    public static UpdateOptions ofColumns(String columns) {
        return new UpdateOptions().setUpdateColumns(columns);
    }

    public static UpdateOptions ofColumns(String[] columns) {
        return new UpdateOptions().setUpdateColumns(columns);
    }

    public static UpdateOptions ofBatchSize(int batchSize) {
        return new UpdateOptions().setBatchSize(batchSize);
    }

    public static UpdateOptions ofIgnoreNullValues(boolean b) {
        return new UpdateOptions().setIgnoreNullValues(b);
    }

    public String getTable() {
        return table;
    }

    public String getTable(String def) {
        Objects.requireNonNull(def);
        return table == null ? def : table;
    }

    public UpdateOptions setTable(String table) {
        check();
        this.table = table;
        return this;
    }

    public String[] getUpdateColumns() {
        return columns;
    }


    public String[] getUpdateColumns(String[] def) {
        return ObjectUtils.isEmpty(columns) ? def : columns;
    }

    public UpdateOptions setUpdateColumns(String[] columns) {
        check();
        this.columns = Objects.requireNonNull(columns);
        return this;
    }

    public UpdateOptions setUpdateColumns(String columns) {
        check();
        this.columns = StringUtils.splitToArray(columns);
        return this;
    }

    public boolean isIgnoreNullValues() {
        return ignoreNullValues;
    }

    public UpdateOptions ignoreNullValues() {
        return setIgnoreNullValues(true);
    }

    public UpdateOptions setIgnoreNullValues(boolean b) {
        check();
        this.ignoreNullValues = b;
        return this;
    }

    public UpdateOptions setBatchSize(int batchSize) {
        check();
        this.batchSize = batchSize;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public SqlHandler getSqlHandler() {
        return sqlHandler;
    }

    public UpdateOptions setSqlHandler(SqlHandler sqlHandler) {
        check();
        this.sqlHandler = sqlHandler;
        return this;
    }

    public GeneratedKeysConsumer getGeneratedKeysConsumer() {
        return generatedKeysConsumer;
    }

    public void setGeneratedKeysConsumer(GeneratedKeysConsumer generatedKeysConsumer) {
        check();
        this.generatedKeysConsumer = generatedKeysConsumer;
    }

    public boolean isReadOnly() {
        return this == DEFAULT;
    }

    private void check() {
        if (DEFAULT == this) {
            throw new UnsupportedOperationException("当前对象是只读的");
        }
    }

}
