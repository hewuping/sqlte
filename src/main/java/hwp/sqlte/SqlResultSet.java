package hwp.sqlte;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class SqlResultSet implements Iterable<Row> {

    //cache
    private String sql;
    private Object[] args;

    private List<String> columns;
    private List<Row> rows;

    public static final SqlResultSet EMPTY = new SqlResultSet(
            Collections.unmodifiableList(Collections.emptyList()),
            Collections.unmodifiableList(Collections.emptyList()));

    public SqlResultSet(List<String> columns, List<Row> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Row firstRow() {
        if (this.rows != null && this.rows.size() > 0) {
            return this.rows.get(0);
        }
        return null;
    }

    public <T> T first(Supplier<T> supplier) {
        Row row = firstRow();
        if (row == null) {
            return null;
        }
        row.convert(supplier.get());
        return null;
    }

    public <T> List<T> map(RowMapper<T> mapper) {
        List<T> list = new ArrayList<>(this.rows.size());
        this.rows.forEach(row -> list.add(mapper.map(row)));
        return list;
    }

    public <T> List<T> map(Supplier<T> supplier) {
        List<T> list = new ArrayList<>(this.rows.size());
        this.rows.forEach(row -> list.add(row.convert(supplier.get())));
        return list;
    }

    protected void unmodifiableRows() {
        this.rows = Collections.unmodifiableList(this.rows);
    }

    @Override
    public Iterator<Row> iterator() {
        return this.rows.iterator();
    }

    @Override
    public void forEach(Consumer<? super Row> action) {
        this.rows.forEach(action);
    }

    @Override
    public Spliterator<Row> spliterator() {
        return this.rows.spliterator();
    }

    public Stream<Row> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public <T> Optional<T> first(RowMapper<T> mapper) throws SQLException {
        Row row = firstRow();
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(row.map(mapper));
    }

    public void putCache(Cache cache) {

    }

}
