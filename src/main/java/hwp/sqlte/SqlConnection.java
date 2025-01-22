package hwp.sqlte;

import hwp.sqlte.util.ClassUtils;

import java.io.Reader;
import java.io.Serializable;
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
     * <pre>{@code
     * conn.queryPage(Group.class, sql -> {
     *     sql.select(Group.class).paging(1, 10);
     * }, (group, row) -> {
     *     group.users = getUsers(group.id);
     * });
     * }</pre>
     *
     * @param clazz
     * @param consumer
     * @param then
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> Page<T> queryPage(Class<T> clazz, Consumer<SqlBuilder> consumer, BiConsumer<T, Row> then) throws SqlteException {
        return queryPage(consumer, Helper.toSupplier(clazz), then);
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
        return this.queryPage(consumer, supplier, null);
    }

    /**
     * 分页查询
     *
     * @param consumer
     * @param supplier
     * @param then     对查询结果再处理
     * @param <T>
     * @return
     * @throws SqlteException
     */
    default <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Supplier<T> supplier, BiConsumer<T, Row> then) throws SqlteException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        String sql = sb.sql();
        int form = sql.lastIndexOf("LIMIT ");
        if (form == -1) {
            throw new IllegalArgumentException("Limit clause not found: " + sql);
        }
        List<T> list = query(sql, sb.args()).list(supplier, then);
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
     * <p>
     * 例如统计18岁的妹子人数: {@code selectCount(new User(FEMALE, 18))}
     *
     * @param example
     * @return
     * @throws SqlteException
     */
    long selectCount(Object example) throws SqlteException;


    /**
     * {@code SELECT EXISTS( SqlBuilder )}
     *
     * @param consumer
     * @return
     * @throws SqlteException
     */
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
     * 根据 Example 查询表总记录数 (仅适用于单表), 同 {@link #selectCount(Object)}
     * <p>
     * 例如统计18岁的妹子人数: {@code count(new User(FEMALE, 18))}
     *
     * @param example
     * @return
     * @throws SqlteException
     */
    default long count(Object example) throws SqlteException {
        return selectCount(example);
    }

    /**
     * 查询全表 (不推荐, 仅数据量特别少时可用)
     *
     * <pre>{@code
     *   List<Employee> list = conn.listAll(Employee.class);
     * } </pre>
     *
     * @param clazz
     * @param <T>
     * @return
     * @since 0.2.28
     */
    default <T> List<T> listAll(Class<T> clazz) {
        return this.list(clazz, Where.EMPTY);
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
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return
     */
    <T> List<T> list(Class<T> clazz, Consumer<Where> consumer);

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
    <T> List<T> list(Class<T> clazz, Collection<? extends Serializable> ids);

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
     *     user.username="xxx";
     * });
     * </pre></blockquote>
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return
     */
    <T> T first(Class<T> clazz, Consumer<T> consumer);

    /**
     * <blockquote><pre>
     * firstExample(User::new, user -> {
     *     user.username="xxx";
     * });
     * </pre></blockquote>
     *
     * @param supplier 查询对象, 比如: User::new
     * @param consumer 查询条件
     * @param <T>
     * @return
     */
    <T> T first(Supplier<T> supplier, Consumer<T> consumer);

    /**
     * 通过 Example 查询最先匹配到的记录
     *
     * @param example
     * @param <T>
     * @return
     */
    <T> T firstExample(T example);


    /**
     * 查找相似数据并返回 List (谨慎使用, 确保数据量小)
     *
     * @param example
     * @param <T>
     * @return
     */
    <T> List<T> listExample(T example);

    /**
     * 查找相似数据并返回 List (谨慎使用, 确保数据量小)
     *
     * @param example  查询条件
     * @param consumer 对查询条件的进一步处理
     * @param <T>
     * @return
     */
    <T> List<T> listExample(Class<T> clazz, Consumer<T> consumer);

    /**
     * 查找相似数据并返回 List (谨慎使用, 确保数据量小)
     *
     * @param clazz   返回的类型
     * @param example 查询条件
     * @param <T>
     * @return
     */
    default <T> List<T> listExample(Class<T> clazz, Object example) {
        return list(clazz, where -> where.of(example));
    }

    /**
     * 插入单条数据
     * <p>
     * 例如: <code>conn.insert("users", "username,email,...", "may", "may@example.com", "...");</code>
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
     * @deprecated 请使用 {@link #executeUpdate(Consumer)} 替代
     */
    default int update(Consumer<SqlBuilder> consumer) throws SqlteException {
        return executeUpdate(consumer);
    }

    /**
     * 更新内容
     *
     * @param table 表名
     * @param data  更新内容
     * @param where 查询条件
     * @return
     * @throws SqlteException
     */
    int update(String table, Map<String, Object> data, Where where) throws SqlteException;

    /**
     * 更新内容
     * <pre>{@code
     * conn.update("users", Map.of("email", "foo@bar.com"), where -> {
     *    where.and("id", 123);
     * });
     * } </pre>
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
     * 更新一行或多行数据
     * <p>
     * 例子:
     * <pre>{@code
     * conn.update("users", data -> {
     *    data.set("email", "zero@example.com");
     * }, where -> {
     *    where.and("user_id=?", 123);
     * });
     * } </pre>
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
        T obj = tryGet(clazz, consumer);
        if (obj == null) {
            throw new NotFoundException("Can't found " + clazz.getSimpleName());
        }
        return obj;
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
    default <T> void insert(T bean) throws SqlteException {
        insert(bean, null);
    }

    /**
     * 插入单条记录到指定表
     *
     * @param bean  插入内容
     * @param table 表名
     * @throws SqlteException
     */
    <T> void insert(T bean, String table) throws SqlteException;

    /**
     * 替换插入到指定表
     *
     * @param bean  插入内容
     * @param table 表名
     * @throws SqlteException
     */
    <T> void replace(T bean, String table) throws SqlteException;

    /**
     * 插入单条记录到指定表并忽略错误
     *
     * @param bean  插入内容
     * @param table 表名
     * @throws SqlteException
     * @deprecated 推荐插入前先检查数据是否存在, 而不是直接执行插入操作
     */
    <T> void insertIgnore(T bean, String table) throws SqlteException;


    /**
     * 更新单条记录
     *
     * @param bean 更新对象
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default <T> boolean update(T bean) throws SqlteException {
        return update(bean, (UpdateOptions) null);
    }


    /**
     * 更新单条记录
     *
     * @param bean    更新对象
     * @param columns 指定更新的列(是表列名, 非类属性名), 多列使用英文逗号分隔
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default <T> boolean update(T bean, String columns) throws SqlteException {
        return this.update(bean, options -> options.setUpdateColumns(columns));
    }

    /**
     * 更新单条记录
     *
     * @param bean             更新对象
     * @param ignoreNullValues 是否忽略值为 null 的字段
     * @return 更新成功返回 true
     * @throws SqlteException
     */
    default <T> boolean update(T bean, boolean ignoreNullValues) throws SqlteException {
        return this.update(bean, options -> options.setIgnoreNullValues(ignoreNullValues));
    }


    default <T> boolean update(T bean, Consumer<UpdateOptions> consumer) throws SqlteException {
        UpdateOptions options = new UpdateOptions();
        consumer.accept(options);
        return update(bean, options);
    }

    <T> boolean update(T bean, UpdateOptions options) throws SqlteException;

    /**
     * 插入或更新
     * <pre>{@code
     * conn.save(user, it -> {
     *   return it.id == null ? Action.INSERT : Action.UPDATE;
     * });
     * } </pre>
     * <p>
     * 效果等同下面代码
     * <pre>{@code
     * if (user.id == null) {
     *    onn.insert(user);
     * } else {
     *    conn.update(user);
     * }
     * } </pre>
     *
     * @param bean 数据对象
     * @param fn   自定义函数, 返回 Action.INSERT 表示对该对象执行插入操作, 返回 Action.UPDATE 表示该对对象执行更新操作
     * @param <T>
     */
    default <T> void save(T bean, Function<T, Action> fn) {
        Objects.requireNonNull(bean);
        Objects.requireNonNull(fn);
        if (fn.apply(bean) == Action.INSERT) {
            this.insert(bean);
        } else {
            this.update(bean);
        }
    }

    /**
     * 插入或更新, 如果明确是插入/更新, 请使用 batchInsert()/batchUpdate() 方法
     * <pre>{@code
     * List<User> users = ...;
     * conn.saveAll(users, user -> {
     *   return user.id == null ? Action.INSERT : Action.UPDATE;
     * });
     * } </pre>
     *
     * @param beans
     * @param fn
     * @param <T>
     */
    default <T> void saveAll(List<T> beans, Function<T, Action> fn) {
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
    default <T> boolean delete(T bean) throws SqlteException {
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
    <T> boolean delete(T bean, String table) throws SqlteException;

    /**
     * 根据条件删除多条记录 (安全删除)
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
        where.check();
        return this.executeUpdate(sql -> {
            sql.delete(table).where(where);
        });
    }


    /**
     * 根据条件删除多条记录 (安全删除)
     *
     * @param clazz
     * @param whereConsumer 条件, 必须
     * @return
     * @throws SqlteException
     */
    <T> int delete(Class<T> clazz, Consumer<Where> whereConsumer) throws SqlteException;

    /**
     * 根据条件删除多条记录 (安全删除), 全部条件使用 = 和 AND
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
    <T> int deleteByMap(Class<T> clazz, Consumer<Map<String, Object>> whereConsumer) throws SqlteException;


    /**
     * 删除相似数据 (安全删除)
     *
     * @param example
     * @return
     * @throws SqlteException
     */
    default int deleteByExample(Object example) throws SqlteException {
        return this.delete(example.getClass(), where -> where.of(example));
    }

    /**
     * 删除相似数据 (安全删除)
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
     * 批量删除, 根据添加了 {@link Id } 注解的字段和值作为条件
     *
     * @param beans
     * @param <T>
     * @throws SqlteException
     */
    default <T> int batchDelete(List<T> beans) throws SqlteException {
        return this.batchDelete(beans, null);
    }

    /**
     * 批量删除, 根据添加了 {@link Id } 注解的字段和值作为条件
     *
     * @param beans
     * @param table 表名, 可以为 null
     * @param <T>
     * @throws SqlteException
     */
    <T> int batchDelete(List<T> beans, String table) throws SqlteException;


    ////////////////////////////////////////Batch operation//////////////////////////////////////////////////////////


    /**
     * 批量插入
     *
     * @param beans
     * @return
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchInsert(List<T> beans) throws SqlteException {
        // 这里 不使用 UpdateOptions.DEFAULT, 因为 options 可能会被修改
        return batchInsert(beans, UpdateOptions.of());
    }

    /**
     * 批量插入
     *
     * @param beans 不能为 null
     * @param table 如果为 null, 默认值为 list 中的第一个值的类映射的表名
     * @return 返回 BatchUpdateResult
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchInsert(List<T> beans, String table) throws SqlteException {
        return batchInsert(beans, UpdateOptions.ofTable(table));
    }

    /**
     * 批量插入
     *
     * @param beans   不能为 null
     * @param options 不能为 null, 更新选项
     * @param <T>
     * @return
     * @throws SqlteException
     * @since 0.3.0
     */
    <T> BatchUpdateResult batchInsert(List<T> beans, UpdateOptions options) throws SqlteException;

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
    default BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws SqlteException {
        String sql = Helper.makeInsertSql(table, columns);
        return this.batchUpdate(sql, consumer);
    }


    /**
     * 批量插入 (该方法不会返回自动生成的 ID)
     *
     * <pre>{@code
     * conn.batchInsert(User.class, it -> {
     *      for (int i = 0; i < size; i++) {
     *          User user = new User("Frank" + i, "frank@xxx.com", "123456");
     *          user.id = i;
     *          user.updated_time = new Date();
     *          it.accept(user);
     *      }
     * }, UpdateOptions.of());
     * } </pre>
     *
     * @param clazz   数据类型
     * @param loader  数据加载器
     * @param options 更新选项
     * @param <T>
     * @return
     * @throws SqlteException
     * @since 0.3.0
     */
    <T> BatchUpdateResult batchInsert(Class<T> clazz, DataLoader<T> loader, UpdateOptions options) throws SqlteException;


    /**
     * 批量更新(插入/更新/删除)
     * <p>
     * 批量插入例子
     * <blockquote><pre>
     * conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
     *      executor.exec("bb@example.com", "bb");
     *      executor.exec("aa@example.com", "aa");
     * });
     * </pre></blockquote>
     * <p>
     * 批量更新例子
     * <blockquote><pre>
     * conn.batchUpdate("UPDATE xxx SET locked=? WHERE id=?", executor -> {
     *      executor.exec(true, "101");
     *      executor.exec(false, "102");
     * });
     * </pre></blockquote>
     *
     * @param sql      自定义 SQL 语句
     * @param consumer 设置参数
     * @return
     * @throws SqlteException
     */
    default BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws SqlteException {
        return batchUpdate(sql, consumer, UpdateOptions.DEFAULT);
    }

    /**
     * 批量更新(插入/更新/删除)
     * <p>
     * 批量插入的例子
     * <blockquote><pre>
     * conn.batchUpdate("INSERT INTO users (email, username)  VALUES (?, ?)", executor -> {
     *      executor.exec("bb@example.com", "bb");
     *      executor.exec("aa@example.com", "aa");
     * }, UpdateOptions.ofBatchSize(1000));
     * </pre></blockquote>
     *
     * @param sql
     * @param options
     * @return
     * @throws SqlteException
     * @since 0.3.0
     */
    BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer, UpdateOptions options) throws SqlteException;

    /**
     * 批量更新, 将对象列表的数据同步到数据库
     *
     * @param beans
     * @throws SqlteException
     */
    default <T> BatchUpdateResult batchUpdate(List<T> beans) throws SqlteException {
        return batchUpdate(beans, null, null);
    }

    /**
     * 批量更新, 将对象列表的数据同步到数据库中指定的表中
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
     * 批量更新, 将对象列表的数据同步到数据库中指定的表中, 仅同步特定列的数据
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
     * 批量保存(批量插入或更新)
     * <p>
     * 生成的 SQL 是插入还是更新还是混合由第二个参数的返回值决定, 如果同时存在插入和更新,
     * 会分别执行插入和更新语句
     *
     * @param list 保存对象列表, 必需是同一类型
     * @param fn   返回 Action.INSERT 表示对该对象执行插入操作, 返回 Action.UPDATE 表示该对对象执行更新操作
     * @param <T>
     */
    default <T> void batchSave(List<T> list, Function<T, Action> fn) {
        List<T> inserts = new ArrayList<>();
        List<T> updates = new ArrayList<>();
        for (T obj : list) {
            Action action = fn.apply(obj);
            if (Action.INSERT == action) {
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
