package hwp.sqlte;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.EMPTY_LIST;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public class SqlResultSet implements Iterable<Row> {

    /**
     * 查询结果集小写列别名
     */
    private final List<String> columns;
    private List<Row> rows;
    private final List<ColumnMetaData> columnMetaDatas;

    //rowMetadata

    public static final SqlResultSet EMPTY = new SqlResultSet(EMPTY_LIST, EMPTY_LIST, EMPTY_LIST);

    public SqlResultSet(List<String> columns, List<Row> rows, List<ColumnMetaData> columnMetaDatas) {
        this.columns = columns;
        this.rows = rows;
        this.columnMetaDatas = columnMetaDatas;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Row> rows() {
        return rows;
    }

    public List<ColumnMetaData> getRowMetadatas() {
        return columnMetaDatas;
    }

    public Row first() {
        if (this.rows != null && !this.rows.isEmpty()) {
            return this.rows.get(0);
        }
        return null;
    }

    public <T> T first(Supplier<T> supplier) {
        Row row = first();
        if (row == null) {
            return null;
        }
//        return row.map(new BeanMapper<>(supplier));
        return row.copyTo(supplier.get());
    }

    public <T> T first(Supplier<T> supplier, BiConsumer<T, Row> then) {
        T obj = first(supplier);
        Row row = first();
        then.accept(obj, row);
        return obj;
    }

    public <T> T first(Class<T> clazz) {
        return first(clazz, (T) null);
    }

    public <T> T first(Class<T> clazz, BiConsumer<T, Row> then) {
        T obj = first(clazz);
        Row row = first();
        then.accept(obj, row);
        return obj;
    }

    public <T> T first(Class<T> clazz, T def) {
        Row row = first();
        if (row == null || row.values().isEmpty()) {
            return def;
        }
        if (row.size() == 1) {
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
        return row.map(new BeanMapper<>(clazz));
    }

    /**
     * 返回第一行的第一个值 (注意这里默认值为0)
     *
     * @return
     */
    public Integer asInt() {
        return this.first(Integer.class, 0);
    }

    /**
     * 返回第一行的第一个值 (注意这里默认值为0L)
     *
     * @return
     */
    public Long asLong() {
        return this.first(Long.class, 0L);
    }

    public String asString() {
        return this.first(String.class);
    }

    /**
     * 多行数据转为对象列表
     * <pre>{@code
     * list(row -> {
     *     User user = new User();
     *     user.username = row.getString("username");
     *     ...
     *     return user;
     * })
     * } </pre>
     * <p>
     * 比如查询关联数据
     * <pre>{@code
     * list(row -> {
     *     Group group = row.map(Group.class);
     *     group.users = getUsers(row.getInteger("group_id"));
     *     return group;
     * })
     * } </pre>
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public <T> List<T> list(RowMapper<T> mapper) {
        List<T> list = new ArrayList<>(this.rows.size());
        this.rows.forEach(row -> list.add(mapper.map(row)));
        return list;
    }

    /**
     * 多行数据转为对象列表
     *
     * <pre>{@code
     * List<User> users = list(User.class)
     * } </pre>
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> list(Class<T> clazz) {
        RowMapper<T> mapper = RowMapper.getRegistry().getRowMapper(clazz);
        if (mapper != null) {
            return list(mapper);
        }
        if (clazz.isEnum()) {
            return list(new RowMapper.EnumMapper<>(clazz));
        }
        return this.list(new BeanMapper<T>(clazz));
    }

    /**
     * 多行数据转为对象列表, 然后二次处理对象
     * <pre>{@code
     * list(Group.class, (group, row) -> {
     *     group.users = getUsers(row.getInteger("group_id"));
     * })
     * } </pre>
     *
     * @param clazz
     * @param then
     * @param <T>
     * @return
     */
    public <T> List<T> list(Class<T> clazz, BiConsumer<T, Row> then) {
        List<T> list = this.list(clazz);
        for (int i = 0; i < list.size(); i++) {
            then.accept(list.get(i), rows.get(i));
        }
        return list;
    }

    public <T> List<T> list(Supplier<T> supplier) {
        List<T> list = new ArrayList<>(this.rows.size());
        BeanMapper<T> mapper = new BeanMapper<>(supplier);
        this.rows.forEach(row -> list.add(mapper.map(row)));
        return list;
    }

    /**
     * 多行数据转为对象列表, 然后二次处理对象
     * <pre>{@code
     * list(Group::new, (group, row) -> {
     *     group.users = getUsers(row.getInteger("group_id"));
     * })
     * } </pre>
     *
     * @param supplier
     * @param then
     * @param <T>
     * @return
     */
    public <T> List<T> list(Supplier<T> supplier, BiConsumer<T, Row> then) {
        List<T> list = this.list(supplier);
        if (then != null) {
            for (int i = 0; i < list.size(); i++) {
                then.accept(list.get(i), rows.get(i));
            }
        }
        return list;
    }

    public <T> List<T> list(Supplier<T> supplier, Consumer<T> then) {
        List<T> list = this.list(supplier);
        if (then != null) {
            for (int i = 0; i < list.size(); i++) {
                then.accept(list.get(i));
            }
        }
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

    public <T> T first(RowMapper<T> mapper) throws SqlteException {
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

    public boolean isNotEmpty() {
        return !isEmpty();
    }

/*    public void cache(Cache cache) {
        cache.put("", this);
    }*/

}
