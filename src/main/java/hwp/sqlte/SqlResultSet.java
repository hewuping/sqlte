package hwp.sqlte;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public class SqlResultSet implements Iterable<Row> {

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

    public Row first() {
        if (this.rows != null && this.rows.size() > 0) {
            return this.rows.get(0);
        }
        return null;
    }

    public <T> T first(Supplier<T> supplier) {
        Row row = first();
        if (row == null) {
            return null;
        }
        return row.map(new RowMapper.BeanMapper<>(supplier));
    }

    public Integer firstInt() {
        return this.first(RowMapper.INTEGER);
    }

    public Integer firstInt(Integer def) {
        return this.first(RowMapper.INTEGER);
    }

    public Long firstLong() {
        return this.first(RowMapper.LONG);
    }

    public String firstString() {
        return this.first(RowMapper.STRING);
    }

    public <T> List<T> list(RowMapper<T> mapper) {
        List<T> list = new ArrayList<>(this.rows.size());
        this.rows.forEach(row -> list.add(mapper.map(row)));
        return list;
    }

    public <T> List<T> list(Supplier<T> supplier) {
        return this.list(supplier, null);
    }

    public <T> List<T> list(Supplier<T> supplier, Consumer<T> consumer) {
        List<T> list = new ArrayList<>(this.rows.size());
        RowMapper.BeanMapper<T> mapper = new RowMapper.BeanMapper<>(supplier);
        this.rows.forEach(row -> list.add(mapper.map(row)));
        if (consumer != null) {
            for (T obj : list) {
                consumer.accept(obj);
            }
        }
        return list;
    }
/*
    public <T> List<T> list(Class<T> clazz) {
        List<T> list = new ArrayList<>(this.rows.size());
        RowMapper.BeanMapper<T> mapper = new RowMapper.BeanMapper<>(clazz);
        this.rows.forEach(row -> list.add(mapper.map(row)));
        return list;
    }*/

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

    public <T> T first(RowMapper<T> mapper) throws UncheckedSQLException {
        Row row = first();
        if (row == null) {
            return null;
        }
        return row.map(mapper);
    }

    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }

/*    public void cache(Cache cache) {
        cache.put("", this);
    }*/

}
