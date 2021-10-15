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

    public List<Row> rows() {
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

    public <T> T first(Class<T> clazz) {
        return first(clazz, null);
    }

    public <T> T first(Class<T> clazz, T def) {
        Row row = first();
        if (row == null || row.values().isEmpty()) {
            return def;
        }
        if (row.keySet().size() == 1) {
            ConversionService service = Config.getConfig().getConversionService();
            Object v = row.values().iterator().next();
            if (v == null) {
                return def;
            }
            if (clazz.isAssignableFrom(v.getClass())) {
                return (T) v;
            }
            if (service.canConvert(v.getClass(), clazz)) {
                return service.convert(v, clazz);
            }
        }
        return row.map(new RowMapper.BeanMapper<>(clazz));
    }

    public Integer asInt() {
        return this.first(Integer.class, 0);
    }

    public Long asLong() {
        return this.first(Long.class, 0L);
    }

    public String asString() {
        return this.first(String.class);
    }

    public <T> List<T> list(RowMapper<T> mapper) {
        List<T> list = new ArrayList<>(this.rows.size());
        this.rows.forEach(row -> list.add(mapper.map(row)));
        return list;
    }

    public <T> List<T> list(Supplier<T> supplier) {
        return this.list(supplier, null);
    }

    public <T> List<T> list(Class<T> clazz) {
        return this.list(() -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SqlteException(e);
            }
        }, null);
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
        if (row == null || row.values().isEmpty()) {
            return null;
        }
        Object firstValue = row.values().iterator().next();
        if (firstValue == null) {
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
