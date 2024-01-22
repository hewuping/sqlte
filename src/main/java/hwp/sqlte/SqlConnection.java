package hwp.sqlte;

import hwp.sqlte.util.ClassUtils;

import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Zero
 * Created on 2018/10/31.
 */
public interface SqlConnection extends AutoCloseable {

    SqlConnection cacheable();

    SqlResultSet query(String sql, Object... args) throws SqlteException;

    /**
     * <pre>{@code
     *   List<Employee> list= query(Employee.class, where -> {
     *       where.and("salary >= ?", 5000);
     *   });
     * } </pre>
     * <p>
     * <p>
     * 请使用 list 代替
     *
     * <pre>{@code
     *   List<Employee> list= list(Employee.class, where -> {
     *       where.and("salary >= ?", 5000);
     *   });
     * } </pre>
     *
     * @param returnType
     * @param where
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> List<T> query(Class<T> returnType, Consumer<Where> where) throws SqlteException {
        ClassInfo info = ClassInfo.getClassInfo(returnType);
        return query(sql -> sql.from(info.getTableName()).where(where)).list(returnType);
    }

    /**
     * 查询
     *
     * @param sql
     * @return
     * @throws SqlteException
     */
    default SqlResultSet query(String sql) throws SqlteException {
        return query(sql, (Object[]) null);
    }

    /**
     * 查询
     *
     * @param sql
     * @return
     * @throws SqlteException
     */
    default SqlResultSet query(Sql sql) throws SqlteException {
        return query(sql.sql(), sql.args());
    }

    /**
     * 查询
     *
     * @param consumer
     * @return
     * @throws SqlteException
     */
    default SqlResultSet query(Consumer<SqlBuilder> consumer) throws SqlteException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        return query(sb.sql(), sb.args());
    }

    default <T> Page<T> queryPage(Class<T> clazz, Consumer<SqlBuilder> consumer) throws SqlteException {
        return queryPage(consumer, Helper.toSupplier(clazz));
    }

    /**
     * 分页查询
     *
     * @param consumer
     * @param clazz
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Class<T> clazz) throws SqlteException {
        return queryPage(consumer, Helper.toSupplier(clazz));
    }

/*    default <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Class<T> resultClass, long maxCount) throws SqlteException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        String sql = sb.sql();
        int form = sql.lastIndexOf("LIMIT ");
        if (form == -1) {
            throw new IllegalArgumentException("Limit clause not found: " + sql);
        }
        List<T> list = query(sql, sb.args()).list(resultClass);
        String countSql = "SELECT COUNT(*) FROM (" + sql.substring(0, form) + " LIMIT " + maxCount + ") AS _t";
        Long count = query(countSql, sb.args()).first(Long.class);
        return new Page<>(list, count);
    }*/

    /**
     * 分页查询
     *
     * @param consumer
     * @param supplier
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Supplier<T> supplier) throws SqlteException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        String sql = sb.sql();
        int form = sql.lastIndexOf("LIMIT ");
        if (form == -1) {
            throw new IllegalArgumentException("Limit clause not found: " + sql);
        }
        List<T> list = query(sql, sb.args()).list(supplier);
        String countSql = "SELECT COUNT(*) FROM (" + sql.substring(0, form) + ") AS _t";
        Long count = query(countSql, sb.args()).first(Long.class);
        return new Page<>(list, count);
    }

    /**
     * @param sql        sql
     * @param rowHandler return true if continue
     * @throws SqlteException if a database access error occurs
     */
    void query(Sql sql, RowHandler rowHandler) throws SqlteException;

    /**
     * @param consumer   build SQL
     * @param rowHandler return true if continue
     * @throws SqlteException
     */
    default void query(Consumer<SqlBuilder> consumer, RowHandler rowHandler) throws SqlteException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        query(builder, rowHandler);
    }

    /**
     * 查询数据并处理数据
     *
     * @param sql
     * @param rowHandler
     * @throws SqlteException
     */
    void query(Sql sql, ResultSetHandler rowHandler) throws SqlteException;

    /**
     * 查询数据并处理数据
     *
     * @param consumer
     * @param rowHandler
     * @throws SqlteException
     */
    default void query(Consumer<SqlBuilder> consumer, ResultSetHandler rowHandler) throws SqlteException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        query(sb, rowHandler);
    }

    /**
     * 根据条件查询总记录数
     *
     * @param table
     * @param where
     * @return
     * @throws SqlteException
     */
    default long selectCount(String table, Where where) throws SqlteException {
        return query(sql -> sql.selectCount(table).where(where)).asLong();
    }

    /**
     * 根据 Example 查询表总记录数 (仅适用于单表)
     *
     * @param table
     * @param consumer
     * @return
     * @throws SqlteException
     */
    default long selectCount(String table, Consumer<Where> consumer) throws SqlteException {
        Where where = new Where();
        consumer.accept(where);
        return query(sql -> sql.selectCount(table).where(where)).asLong();
    }

    /**
     * 统计相似数据条数
     *
     * @param example
     * @return
     * @throws SqlteException
     */
    long selectCount(Object example) throws SqlteException;


    default boolean selectExists(Consumer<SqlBuilder> consumer) throws SqlteException {
        SqlBuilder sql = new SqlBuilder();
        consumer.accept(sql);
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT EXISTS(");
        builder.append(sql.sql());
        builder.append(")");
        return query(builder.toString(), sql.args()).asLong() == 1;
    }


    /**
     * 根据 Example 查询表总记录数 (仅适用于单表), 同 selectCount(Object example)
     *
     * @param example
     * @return
     * @throws SqlteException
     */
    default long count(Object example) throws SqlteException {
        return selectCount(example);
    }

    /**
     * 查询表数据并返回 List
     *
     * <pre>{@code
     *   List<Employee> list = conn.list(Employee.class, where -> {
     *      where.and("first_name =?", "Foo");
     *   });
     * } </pre>
     * <p>
     * <p>
     * 如果需要查询全表 (不推荐, 仅数据量特别少时可用)
     *
     * <pre>{@code
     *   List<Employee> list = conn.list(Employee.class, Where.EMPTY);
     * } </pre>
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return
     */
    default <T> List<T> list(Class<T> clazz, Consumer<Where> consumer) {
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        return query(sql -> sql.from(info.getTableName()).where(consumer)).list(clazz);
    }

    /**
     * 根据 ID 列表查询表数据并返回 List
     *
     * <pre>{@code
     *   List<Employee> list = conn.list(Employee.class, List.of(1000, 10001));
     * } </pre>
     *
     * @param clazz 该类必须有且仅有一个属性使用 <code>@Id</code> 注解
     * @param ids
     * @param <T>
     * @return
     * @since 0.2.16
     */
    default <T> List<T> list(Class<T> clazz, Collection<? extends Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        String pkColumn = info.getPKColumn();
        return list(clazz, where -> {
            where.and(Condition.in(pkColumn, ids));
        });
    }

    /**
     * 是否存在相似对象
     *
     * @param example
     * @param <T>
     * @return
     */
    default <T> boolean contains(T example) {
        return firstExample(example) != null;
    }


    /**
     * <blockquote><pre>
     * first(User.class, user -> {
     *     user.username=="xxx";
     * });
     * </pre></blockquote>
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return
     */
    default <T> T first(Class<T> clazz, Consumer<T> consumer) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(consumer);
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        T query = ClassUtils.newInstance(clazz);
        consumer.accept(query);
        return query(sql -> sql.from(info.getTableName()).where(query).limit(1)).first(clazz);
    }

    /**
     * <blockquote><pre>
     * firstExample(User::new, user -> {
     *     user.username=="xxx";
     * });
     * </pre></blockquote>
     *
     * @param supplier 查询对象, 比如: User::new
     * @param consumer 查询条件
     * @param <T>
     * @return
     */
    default <T> T first(Supplier<T> supplier, Consumer<T> consumer) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(consumer);
        T example = supplier.get();
        consumer.accept(example);
        ClassInfo info = ClassInfo.getClassInfo(example.getClass());
        return query(sql -> sql.from(info.getTableName()).where(example).limit(1)).first(supplier);
    }

    /**
     * 通过 Example 查询最先匹配到的记录
     *
     * @param example
     * @param <T>
     * @return
     */
    default <T> T firstExample(T example) {
        Objects.requireNonNull(example, "example can't be null");
        Class<T> clazz = (Class<T>) example.getClass();
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        return query(sql -> sql.from(info.getTableName()).where(example).limit(1)).first(clazz);
    }


    /**
     * 查找相似数据并返回 List (谨慎使用, 确保数据量小)
     *
     * @param example
     * @param <T>
     * @return
     */
    default <T> List<T> listExample(T example) {
        Class<T> aClass = (Class<T>) example.getClass();
        return query(aClass, where -> where.of(example));
    }

    /**
     * 查找相似数据并返回 List (谨慎使用, 确保数据量小)
     *
     * @param example  查询条件
     * @param consumer 对查询条件的进一步处理
     * @param <T>
     * @return
     */
    default <T> List<T> listExample(Class<T> clazz, Consumer<T> consumer) {
        T example = ClassUtils.newInstance(clazz);
        consumer.accept(example);
        return listExample(example);
    }

    /**
     * 查找相似数据并返回 List (谨慎使用, 确保数据量小)
     *
     * @param clazz   返回的类型
     * @param example 查询条件
     * @param <T>
     * @return
     */
    default <T> List<T> listExample(Class<T> clazz, Object example) {
        return query(clazz, where -> where.of(example));
    }

    /**
     * 插入单条数据
     * <p>
     * 例如: <code>conn.insert("users", "username,password,password_salt", "may", "123456", "xxx");</code>
     *
     * @param table
     * @param columns
     * @param args
     * @return
     * @throws SqlteException
     */
    int insert(String table, String columns, Object... args) throws SqlteException;

    /**
     * 插入数据
     *
     * @param sql
     * @param resultHandler
     * @throws SqlteException
     */
    void insert(Sql sql, ResultSetHandler resultHandler) throws SqlteException;

    /**
     * 插入数据并返回自动生成的 ID
     *
     * @param sql
     * @param idColumn 自动生成值的列名
     * @param args
     * @return
     * @throws SqlteException
     */
    Long insertAndReturnKey(String sql, String idColumn, Object... args) throws SqlteException;

    /**
     * 通过 Map 插入数据
     *
     * @param table 表名
     * @param row   插入数据内容
     * @return
     * @throws SqlteException
     */
    int insertMap(String table, Map<String, Object> row) throws SqlteException;

    /**
     * 通过 Row 插入数据
     *
     * @param table 表名
     * @param row   插入数据内容
     * @return
     * @throws SqlteException
     */
    int insertMap(String table, Consumer<Row> row) throws SqlteException;

    /**
     * 通过 Row 插入数据
     *
     * @param table
     * @param row
     * @param returnColumns
     * @return 返回受影响的行数
     * @throws SqlteException
     */
    int insertMap(String table, Map<String, Object> row, String... returnColumns) throws SqlteException;

    /**
     * MySQL: REPLACE INTO
     */
    int replaceMap(String table, Map<String, Object> row, String... returnColumns);

    /**
     * MySQL: INSERT IGNORE INTO
     */
    int insertIgnoreMap(String table, Map<String, Object> row, String... returnColumns);

    /**
     * 执行更新 SQL
     *
     * @param sql
     * @param args
     * @return
     * @throws SqlteException
     */
    int executeUpdate(String sql, Object... args) throws SqlteException;//execute

    /**
     * 执行更新 SQL
     *
     * @param consumer
     * @return
     * @throws SqlteException
     */
    default int executeUpdate(Consumer<SqlBuilder> consumer) throws SqlteException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        return this.executeUpdate(builder.sql(), builder.args());
    }

    /**
     * 执行更新 SQL
     *
     * @param consumer
     * @return
     * @throws SqlteException
     */
    int update(Consumer<SqlBuilder> consumer) throws SqlteException;

    /**
     * 更新内容
     *
     * @param table 表名
     * @param map   更新内容
     * @param where 查询条件
     * @return
     * @throws SqlteException
     */
    int update(String table, Map<String, Object> map, Where where) throws SqlteException;

    /**
     * 更新内容
     *
     * @param table 表名
     * @param map   更新内容
     * @param where 查询条件
     * @return
     * @throws SqlteException
     */
    default int update(String table, Map<String, Object> map, Consumer<Where> where) throws SqlteException {
        Where w = new Where();
        where.accept(w);
        return update(table, map, w);
    }

    /**
     * 更新内容
     *
     * @param table    表名
     * @param consumer 更新内容
     * @param where    查询条件
     * @return
     * @throws SqlteException
     */
    default int update(String table, Consumer<Row> consumer, Consumer<Where> where) throws SqlteException {
        Where w = new Where();
        where.accept(w);
        Row row = new Row();
        consumer.accept(row);
        return update(table, row, w);
    }

    /**
     * 更新表数据
     *
     * @param table 更新表
     * @param map   更新内容(需要包含主键)
     * @param pk    主键列名
     * @return
     * @throws SqlteException
     */
    int updateByPks(String table, Map<String, Object> map, String... pk) throws SqlteException;

    ////////////////////////////////////////Simple ORM//////////////////////////////////////////////////////////
//    <T> List<T> query(Supplier<T> supplier, Consumer<SqlBuilder> sql);

    /**
     * 查询数据, 如果数据不存在则返回 null
     *
     * @param supplier
     * @param id
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> T tryGet(Supplier<T> supplier, Object id) throws SqlteException;

    /**
     * 查询数据, 如果数据不存在则返回 null
     *
     * @param clazz
     * @param id
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> T tryGet(Class<T> clazz, Object id) throws SqlteException;

    /**
     * 查询数据(使用联合主键/复合主键), 如果数据不存在则返回 null
     *
     * @param clazz    返回对象类型
     * @param consumer 通过 Map 构建查询条件
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> T tryGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws SqlteException;

    /**
     * 查询数据, 如果数据不存在则抛异常
     *
     * @param clazz
     * @param id
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> T mustGet(Class<T> clazz, Object id) throws SqlteException {
        T obj = tryGet(clazz, id);
        if (obj == null) {
            throw new NotFoundException("Can't found " + clazz.getSimpleName() + " by ID : " + id);
        }
        return obj;
    }

    /**
     * 查询数据(使用联合主键/复合主键), 如果数据不存在则抛异常
     *
     * @param clazz    返回对象类型
     * @param consumer 通过 Map 构建查询条件
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> T mustGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws SqlteException {
        return tryGet(clazz, consumer);
    }

    /**
     * 加载数据, 同 {@code  mustGet() }
     *
     * @param clazz
     * @param id
     * @param <T>
     * @return
     */
    default <T> T load(Class<T> clazz, Object id) {
        return mustGet(clazz, id);
    }

//    Row firstRow(Class<?> clazz, Object id);

    <T, E> E loadAs(Class<T> clazz, Class<E> as, Object id);

    /**
     * 重新加载数据库中的数据到指定对象
     *
     * @param bean
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> T reload(T bean) throws SqlteException;


    /**
     * 重新加载数据库中的数据到指定对象
     *
     * @param bean
     * @param table 如果使用了分表设计, 可以通过该参数指定分表
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> T reload(T bean, String table) throws SqlteException;


    /**
     * 插入单条记录
     *
     * @param bean 插入内容
     * @throws SqlteException
     */
    default void insert(Object bean) throws SqlteException {
        insert(bean, null);
    }

    /**
     * 插入单条记录到指定表
     *
     * @param bean  插入内容
     * @param table 表名
     * @throws SqlteException
     */
    void insert(Object bean, String table) throws SqlteException;

    /**
     * 替换插入到指定表
     *
     * @param bean  插入内容
     * @param table 表名
     * @throws SqlteException
     */
    void replace(Object bean, String table) throws SqlteException;

    /**
     * 插入单条记录到指定表并忽略错误
     *
     * @param bean  插入内容
     * @param table 表名
     * @throws SqlteException
     */
    void insertIgnore(Object bean, String table) throws SqlteException;

    /**
     * 根据条件更新多条记录
     *
     * @param bean            更新内容
     * @param table           表名, 可以为 null
     * @param columns         指定更新的列(是表列名, 非类属性名), 多列使用英文逗号分隔
     * @param ignoreNullValue 是否 bean 中忽略值为 null 的字段
     * @param where           条件
     * @return
     * @throws SqlteException
     */
    boolean update(Object bean, String table, String columns, boolean ignoreNullValue, Consumer<Where> where) throws SqlteException;

    /**
     * 根据条件更新多条记录
     *
     * @param bean  更新内容
     * @param table 表名, 可以为 null
     * @param where 条件
     * @return
     * @throws SqlteException
     */
    default boolean update(Object bean, String table, Consumer<Where> where) throws SqlteException {
        return update(bean, table, null, false, where);
    }

    /**
     * 更新单条记录
     *
     * @param bean            更新对象
     * @param table           表名, 可以为 null
     * @param columns         指定更新的列(是表列名, 非类属性名), 多列使用英文逗号分隔
     * @param ignoreNullValue 是否 bean 中忽略值为 null 的字段
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default boolean update(Object bean, String table, String columns, boolean ignoreNullValue) throws SqlteException {
        return update(bean, table, columns, ignoreNullValue, null);
    }

    /**
     * 更新单条记录
     *
     * @param bean            更新对象
     * @param columns         指定更新的列(是表列名, 非类属性名), 多列使用英文逗号分隔
     * @param ignoreNullValue 是否 bean 中忽略值为 null 的字段
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default boolean update(Object bean, String columns, boolean ignoreNullValue) throws SqlteException {
        return update(bean, null, columns, ignoreNullValue);
    }

    /**
     * 更新单条记录
     *
     * @param bean    更新对象
     * @param columns 指定更新的列(是表列名, 非类属性名), 多列使用英文逗号分隔
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default boolean update(Object bean, String columns) throws SqlteException {
        return this.update(bean, columns, false);
    }

    /**
     * 更新单条记录
     *
     * @param bean            更新对象
     * @param ignoreNullValue 是否 bean 中忽略值为 null 的字段
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default boolean update(Object bean, boolean ignoreNullValue) throws SqlteException {
        return this.update(bean, null, ignoreNullValue);
    }

    /**
     * 更新单条记录
     *
     * @param bean 更新对象
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default boolean update(Object bean) throws SqlteException {
        return update(bean, null, false);
    }

    /**
     * 插入或更新
     *
     * @param bean 数据对象
     * @param fn   自定义函数, 返回 true 表示插入, 返回 false 表示更新
     * @param <T>
     */
    default <T> void save(T bean, Function<T, Boolean> fn) {
        Objects.requireNonNull(bean);
        Objects.requireNonNull(fn);
        if (fn.apply(bean)) {
            this.insert(bean);
        } else {
            this.update(bean);
        }
    }

    /**
     * 插入或更新, 如果明确是插入/更新, 请使用 batchInsert()/batchUpdate() 方法
     *
     * @param beans
     * @param fn
     * @param <T>
     */
    default <T> void save(List<T> beans, Function<T, Boolean> fn) {
        Objects.requireNonNull(beans);
        Objects.requireNonNull(fn);
        for (T bean : beans) {
            this.save(bean, fn);
        }
    }

    //  boolean update(Object bean, String table, Consumer<Where> where) throws SqlteException;

    /**
     * 删除单条记录
     *
     * @param bean
     * @return
     * @throws SqlteException
     */
    default boolean delete(Object bean) throws SqlteException {
        return delete(bean, null);
    }

    /**
     * 删除单条记录
     *
     * @param bean  删除对象, 不能为 null
     * @param table 表名, 可以为 null
     * @return
     * @throws SqlteException
     */
    boolean delete(Object bean, String table) throws SqlteException;

    /**
     * 根据条件删除多条记录
     *
     * @param table         表名, 必须
     * @param whereConsumer 条件, 必须
     * @return
     * @throws SqlteException
     */
    default int delete(String table, Consumer<Where> whereConsumer) throws SqlteException {
        Objects.requireNonNull(table);
        Objects.requireNonNull(whereConsumer);
        Where where = new Where();
        whereConsumer.accept(where);
        if (where.isEmpty() && whereConsumer != Where.EMPTY) {
            throw new IllegalArgumentException("Dangerous deletion without cause is not supported");
        }
        return this.executeUpdate(sql -> {
            sql.delete(table).where(where);
        });
    }


    /**
     * 根据条件删除多条记录
     *
     * @param clazz
     * @param whereConsumer 条件, 必须
     * @return
     * @throws SqlteException
     */
    default int delete(Class<?> clazz, Consumer<Where> whereConsumer) throws SqlteException {
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        return this.delete(info.getTableName(), whereConsumer);
    }

    /**
     * 根据条件删除多条记录, 全部条件使用 = 和 AND
     * <pre>{@code
     *  conn.deleteByMap(User.class, map->{
     *      map.put("age", 18);
     *      map.put("username", "foo");
     *  })
     * } </pre>
     *
     * @param clazz
     * @param whereConsumer
     * @return
     * @throws SqlteException
     */
    default int deleteByMap(Class<?> clazz, Consumer<Map<String, Object>> whereConsumer) throws SqlteException {
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        Map<String, Object> map = new LinkedHashMap<>();
        whereConsumer.accept(map);
        return this.delete(info.getTableName(), where -> where.and(map));
    }


    /**
     * 删除相似数据
     *
     * @param example
     * @return
     * @throws SqlteException
     */
    default int deleteByExample(Object example) throws SqlteException {
        return this.delete(example.getClass(), where -> where.of(example));
    }

    /**
     * 删除相似数据
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> int deleteByExample(Class<T> clazz, Consumer<T> consumer) throws SqlteException {
        T example = ClassUtils.newInstance(clazz);
        consumer.accept(example);
        return this.delete(clazz, where -> where.of(example));
    }

    /**
     * 删除相似数据
     *
     * @param supplier
     * @param consumer
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> int deleteByExample(Supplier<T> supplier, Consumer<T> consumer) throws SqlteException {
        T example = supplier.get();
        consumer.accept(example);
        return this.delete(example.getClass(), where -> where.of(example));
    }


    /**
     * 批量删除
     *
     * @param beans
     * @param <T>
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchDelete(List<T> beans) throws SqlteException {
        return this.batchDelete(beans, null);
    }

    /**
     * 批量删除
     *
     * @param beans
     * @param table 表名, 可以为 null
     * @param <T>
     * @throws SqlteException
     */
    <T> BatchUpdateResult batchDelete(List<T> beans, String table) throws SqlteException;


    ////////////////////////////////////////Batch operation//////////////////////////////////////////////////////////

    /**
     * 批量更新
     *
     * @param sql
     * @param it
     * @param consumer
     * @param <T>
     * @throws SqlteException
     */
    <T> BatchUpdateResult batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws SqlteException;

    /**
     * 批量插入
     *
     * @param beans
     * @return
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchInsert(List<T> beans) throws SqlteException {
        return batchInsert(beans, null);
    }

    /**
     * 批量插入
     *
     * @param beans 不能为 null
     * @param table 如果为 null, 会取 list 中的第一个对象映射的表名
     * @return 返回 BatchUpdateResult
     * @throws SqlteException
     */
    <T> BatchUpdateResult batchInsert(List<T> beans, String table) throws SqlteException;

    /**
     * 批量插入
     *
     * @param beans      不能为 null
     * @param table      如果为 null, 会取 list 中的第一个对象映射的表名
     * @param sqlHandler
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> BatchUpdateResult batchInsert(List<T> beans, String table, SqlHandler sqlHandler) throws SqlteException;

    /**
     * 批量插入 (该方法不会返回自动生成的 ID)
     *
     * <pre>{@code
     * conn.batchInsert(it -> {
     *      for (int i = 0; i < size; i++) {
     *          User user = new User("Frank" + i, "frank@xxx.com", "123456");
     *          user.id = i;
     *          user.updated_time = new Date();
     *          it.accept(user);
     *      }
     * }, User.class, "users");
     * } </pre>
     *
     * @param loader
     * @param clazz
     * @param table
     * @param <T>
     * @return 返回 BatchUpdateResult
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table) throws SqlteException {
        return batchInsert(loader, clazz, table, null, null);
    }

    /**
     * 批量插入 (该方法不会返回自动生成的 ID)
     *
     * <pre>{@code
     * conn.batchInsert(it -> {
     *      for (int i = 0; i < size; i++) {
     *          User user = new User("Frank" + i, "frank@xxx.com", "123456");
     *          user.id = i;
     *          user.updated_time = new Date();
     *          it.accept(user);
     *      }
     * }, User.class, "users", null);
     * } </pre>
     *
     * @param loader
     * @param clazz
     * @param table
     * @param sqlHandler
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler) throws SqlteException {
        return batchInsert(loader, clazz, table, sqlHandler, null);
    }

    /**
     * 批量插入 (该方法不会返回自动生成的 ID)
     */
    <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler, BiConsumer<PreparedStatement, int[]> psConsumer) throws SqlteException;

    /**
     * 批量更新
     *
     * @param sql       更新 SQL 语句
     * @param batchSize 批次大小
     * @param it        参数源
     * @param consumer  设置参数
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> BatchUpdateResult batchUpdate(String sql, int batchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws SqlteException;

    /**
     * 批量更新
     * <blockquote><pre>
     * conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
     *      executor.exec("bb@example.com", "bb");
     *      executor.exec("aa@example.com", "aa");
     * });
     * </pre></blockquote>
     *
     * @param sql      更新 SQL 语句
     * @param consumer 设置参数
     * @return
     * @throws SqlteException
     */
    BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws SqlteException;

    /**
     * 批量更新
     *
     * @param table
     * @param columns
     * @param whereConsumer
     * @param consumer
     * @return
     * @throws SqlteException
     */
    BatchUpdateResult batchUpdate(String table, String columns, Consumer<Where> whereConsumer, Consumer<BatchExecutor> consumer) throws SqlteException;

    /**
     * 批量插入, 例如:
     * <blockquote><pre>
     * conn.batchInsert("users", "email, username", executor -> {
     *      executor.exec("bb@example.com", "bb");
     *      executor.exec("aa@example.com", "aa");
     * });
     * </pre></blockquote>
     *
     * @param table
     * @param columns
     * @param consumer
     * @return
     * @throws SqlteException
     */
    BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws SqlteException;

    /**
     * 批量更新
     *
     * @param sql
     * @param batchSize
     * @param consumer
     * @return
     * @throws SqlteException
     */
    BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer) throws SqlteException;

    /**
     * 批量更新
     *
     * @param sql
     * @param batchSize
     * @param consumer
     * @param psConsumer
     * @return
     * @throws SqlteException
     */
    BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws
            SqlteException;

    /**
     * 批量更新
     *
     * @param statement
     * @param batchSize
     * @param consumer
     * @param psConsumer
     * @return
     * @throws SqlteException
     */
    BatchUpdateResult batchUpdate(PreparedStatement statement, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws SqlteException;

    /**
     * 批量更新
     *
     * @param beans
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchUpdate(List<T> beans) throws SqlteException {
        return batchUpdate(beans, null, null);
    }

    /**
     * 批量更新
     *
     * @param beans 更新的对象列表, 必需是同一类型
     * @param table 表名, 可以为 null, 如果为 null 则去列表中第一个对象的 class 名作为表名
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchUpdate(List<T> beans, String table) throws SqlteException {
        return batchUpdate(beans, table, null);
    }

    /**
     * 批量更新
     *
     * @param beans   更新的对象列表, 必需是同一类型
     * @param table   表名, 可以为 null, 如果为 null 则去列表中第一个对象的 class 名作为表名
     * @param columns 指定更新的列名, 更新多列使用英文逗号分隔
     * @param <T>
     * @return
     * @throws SqlteException
     */
    <T> BatchUpdateResult batchUpdate(List<T> beans, String table, String columns) throws SqlteException;

    /**
     * 批量更新
     * <p>
     * 查询数据并逐行转为对象, 然后对对象进行修改, 最后自动更新到数据库
     *
     * @param clazz       行映射类
     * @param sqlConsumer 查询SQL 构建器
     * @param fun         更新操作
     * @param <T>
     * @throws SqlteException
     * @since 0.2.14
     */
    default <T> void batchUpdate(Class<T> clazz, Consumer<SqlBuilder> sqlConsumer, Predicate<T> fun) throws SqlteException {
        BatchAction<T> action = new BatchAction<>(100, batch -> {
            batchUpdate(batch);
        });
        query(sqlConsumer, (RowHandler) row -> {
            T obj = row.map(clazz);
            if (fun.test(obj)) {
                action.add(obj);
            }
            return true;
        });
        action.end();
    }

    /**
     * @param query  查询条件 Example
     * @param update 更新字段, 非 null 值
     * @param <T>
     * @throws SqlteException
     * @since 0.2.24
    default <T> void batchUpdate(T query, Consumer<T> update) throws SqlteException {
    ClassInfo info = ClassInfo.getClassInfo(query.getClass());
    Field[] fields = info.getFields();
    List<String> columns = new ArrayList<>();
    List<Object> values = new ArrayList<>();
    try {
    for (Field field : fields) {
    String column = info.getColumn(field);
    Object value = field.get(update);
    if (value != null) {
    columns.add(column);
    values.add(value);
    }
    }
    } catch (IllegalAccessException e) {

    }
    executeUpdate(sql -> {
    sql.update(info.getTableName(), String.join(",", columns), values.toArray()).where(query);
    });
    }*/


    /**
     * 批量保存
     * <p>
     * 批量插入或更新, 生成的 SQL 是插入还是更新还是混合由第二个参数决定, 如果同时存在插入和更新, 会分组再执行
     *
     * @param list     保存对象列表, 必需是同一类型
     * @param isInsert 返回 true 表示插入, false 表示更新
     * @param <T>
     */
    default <T> void batchSave(List<T> list, Function<T, Boolean> isInsert) {
        List<T> inserts = new ArrayList<>();
        List<T> updates = new ArrayList<>();
        for (T obj : list) {
            Boolean b = isInsert.apply(obj);
            if (b != null && b) {
                inserts.add(obj);
            } else {
                updates.add(obj);
            }
        }
        if (!inserts.isEmpty()) {
            batchInsert(inserts);
        }
        if (!updates.isEmpty()) {
            batchUpdate(updates);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

//    void setQueryCache(boolean b);

    void executeSqlScript(Reader reader, boolean ignoreError);

    void statement(Consumer<Statement> consumer) throws SqlteException;

    void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws SqlteException;

    void setAutoCommit(boolean autoCommit) throws SqlteException;

    boolean getAutoCommit() throws SqlteException;

    void commit() throws SqlteException;

    void rollback() throws SqlteException;

    void close() throws SqlteException;

    boolean isClosed() throws SqlteException;

    void setReadOnly(boolean readOnly) throws SqlteException;

    boolean isReadOnly() throws SqlteException;

    void setTransactionIsolation(int level) throws SqlteException;

    int getTransactionIsolation() throws SqlteException;

    SqlConnection beginTransaction() throws SqlteException;

    SqlConnection beginTransaction(int level) throws SqlteException;

    Savepoint setSavepoint() throws SqlteException;

    Savepoint setSavepoint(String name) throws SqlteException;

    void rollback(Savepoint savepoint) throws SqlteException;

    void releaseSavepoint(Savepoint savepoint) throws SqlteException;

    PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SqlteException;

    PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SqlteException;

    PreparedStatement prepareStatement(String sql, String[] columnNames) throws SqlteException;

    boolean isValid(int timeout) throws SqlteException;

    PreparedStatement prepareStatement(String sql) throws SqlteException;

    CallableStatement prepareCall(String sql) throws SqlteException;

    CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SqlteException;

    Connection connection();
}
