package hwp.sqlte;

import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

/**
 * @author Zero
 * Created on 2018/10/31.
 */
public interface SqlConnection extends AutoCloseable {

    SqlConnection cacheable();

    SqlResultSet query(String sql, Object... args) throws UncheckedSQLException;

    default <T> SqlResultSet query(Class<T> returnType, Consumer<Where> where) throws UncheckedSQLException {
        ClassInfo info = ClassInfo.getClassInfo(returnType);
        return query(sql -> sql.from(info.getTableName()).where(where));
    }

    default SqlResultSet query(String sql) throws UncheckedSQLException {
        return query(sql, (Object[]) null);
    }

    default SqlResultSet query(Sql sql) throws UncheckedSQLException {
        return query(sql.sql(), sql.args());
    }

    default SqlResultSet query(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        return query(sb.sql(), sb.args());
    }

    default <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Class<T> clazz) throws UncheckedSQLException {
        return queryPage(consumer, () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    default <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Supplier<T> supplier) throws UncheckedSQLException {
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
     * @throws UncheckedSQLException if a database access error occurs
     */
    void query(Sql sql, RowHandler rowHandler) throws UncheckedSQLException;

    /**
     * @param consumer   build SQL
     * @param rowHandler return true if continue
     * @throws UncheckedSQLException
     */
    default void query(Consumer<SqlBuilder> consumer, RowHandler rowHandler) throws UncheckedSQLException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        query(builder, rowHandler);
    }


    void query(Sql sql, ResultSetHandler rowHandler) throws UncheckedSQLException;

    default void query(Consumer<SqlBuilder> consumer, ResultSetHandler rowHandler) throws UncheckedSQLException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        query(sb, rowHandler);
    }

    default long selectCount(String table, Where where) throws UncheckedSQLException {
        return query(sql -> sql.selectCount(table).where(where)).first(Long.class);
    }

    default boolean selectExists(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder sql = new SqlBuilder();
        consumer.accept(sql);
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT EXISTS(");
        builder.append(sql.sql());
        builder.append(")");
        return query(builder.toString(), sql.args()).first(Long.class) == 1;
    }

    default <T> List<T> listAll(Class<T> clazz) {
        return list(clazz, null);
    }

    default <T> List<T> list(Class<T> clazz, Consumer<Where> consumer) {
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        return query(sql -> sql.from(info.getTableName()).where(consumer)).list(clazz);
    }

    default <T> T firstExample(T example) {
        Objects.requireNonNull(example, "example can't be null");
        Class<T> clazz = (Class<T>) example.getClass();
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        return query(sql -> sql.from(info.getTableName()).where(example).limit(1)).first(clazz);
    }

    int insert(String table, String columns, Object... args) throws UncheckedSQLException;

    void insert(Sql sql, ResultSetHandler resultHandler) throws UncheckedSQLException;

    Long insertAndReturnKey(String sql, String idColumn, Object... args) throws UncheckedSQLException;


    int insertMap(String table, Map<String, Object> row) throws UncheckedSQLException;

    int insertMap(String table, Consumer<Row> row) throws UncheckedSQLException;

    int insertMap(String table, Map<String, Object> row, String... returnColumns) throws UncheckedSQLException;

    /**
     * MySQL: REPLACE INTO
     */
    int replaceMap(String table, Map<String, Object> row, String... returnColumns);

    /**
     * MySQL: INSERT IGNORE INTO
     */
    int insertIgnoreMap(String table, Map<String, Object> row, String... returnColumns);

    int executeUpdate(String sql, Object... args) throws UncheckedSQLException;//execute

    default int executeUpdate(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        return this.executeUpdate(builder.sql(), builder.args());
    }

    int update(Consumer<SqlBuilder> consumer) throws UncheckedSQLException;

    int update(String table, Map<String, Object> map, Where where) throws UncheckedSQLException;

    default int update(String table, Map<String, Object> map, Consumer<Where> where) throws UncheckedSQLException {
        Where w = new Where();
        where.accept(w);
        return update(table, map, w);
    }

    default int update(String table, Consumer<Row> consumer, Consumer<Where> where) throws UncheckedSQLException {
        Where w = new Where();
        where.accept(w);
        Row row = new Row();
        consumer.accept(row);
        return update(table, row, w);
    }

    int updateByPks(String table, Map<String, Object> map, String... pk) throws UncheckedSQLException;

    ////////////////////////////////////////Simple ORM//////////////////////////////////////////////////////////
//    <T> List<T> query(Supplier<T> supplier, Consumer<SqlBuilder> sql);

    <T> T tryGet(Supplier<T> supplier, Object id) throws UncheckedSQLException;

    <T> T tryGet(Class<T> clazz, Object id) throws UncheckedSQLException;

    <T> T tryGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws UncheckedSQLException;

    default <T> T mustGet(Class<T> clazz, Object id) throws UncheckedSQLException {
        T obj = tryGet(clazz, id);
        if (obj == null) {
            throw new NotFoundException("Can't found " + clazz.getSimpleName() + " by ID : " + id);
        }
        return obj;
    }

    default <T> T mustGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws UncheckedSQLException {
        return tryGet(clazz, consumer);
    }

    <T> T reload(T bean) throws UncheckedSQLException;

    default void insert(Object bean) throws UncheckedSQLException {
        insert(bean, null);
    }

    void insert(Object bean, String table) throws UncheckedSQLException;

    void replace(Object bean, String table) throws UncheckedSQLException;

    void insertIgnore(Object bean, String table) throws UncheckedSQLException;

    boolean update(Object bean, String table, String columns, boolean ignoreNullValue, Consumer<Where> where) throws UncheckedSQLException;

    default boolean update(Object bean, String table, Consumer<Where> where) throws UncheckedSQLException {
        return update(bean, table, null, false, where);
    }

    default boolean update(Object bean, String table, String columns, boolean ignoreNullValue) throws UncheckedSQLException {
        return update(bean, table, columns, ignoreNullValue, null);
    }

    default boolean update(Object bean, String columns, boolean ignoreNullValue) throws UncheckedSQLException {
        return update(bean, null, columns, ignoreNullValue);
    }

    default boolean update(Object bean, String columns) throws UncheckedSQLException {
        return this.update(bean, columns, false);
    }

    default boolean update(Object bean, boolean ignoreNullValue) throws UncheckedSQLException {
        return this.update(bean, null, ignoreNullValue);
    }

    default boolean update(Object bean) throws UncheckedSQLException {
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

    //  boolean update(Object bean, String table, Consumer<Where> where) throws UncheckedSQLException;

    default boolean delete(Object bean) throws UncheckedSQLException {
        return delete(bean, null);
    }

    boolean delete(Object bean, String table) throws UncheckedSQLException;

    default int delete(String table, Consumer<Where> whereConsumer) throws UncheckedSQLException {
        Where where = new Where();
        whereConsumer.accept(where);
        if (where.isEmpty()) {
            throw new IllegalArgumentException("Dangerous deletion without cause is not supported");
        }
        return this.executeUpdate("DELETE FROM " + table + " WHERE " + where.sql(), where.args().toArray());
    }

    ////////////////////////////////////////Batch operation//////////////////////////////////////////////////////////

    <T> void batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException;

    /**
     * @param beans
     * @return
     * @throws UncheckedSQLException
     */
    default BatchUpdateResult batchInsert(List<?> beans) throws UncheckedSQLException {
        return batchInsert(beans, null);
    }

    /**
     * @param beans
     * @param table 如果为 null, 会区 list 中的第一个对象映射的表名
     * @return
     * @throws UncheckedSQLException
     */
    BatchUpdateResult batchInsert(List<?> beans, String table) throws UncheckedSQLException;

    BatchUpdateResult batchInsert(List<?> beans, String table, SqlHandler sqlHandler) throws UncheckedSQLException;

    /**
     * <blockquote><pre>
     * conn.batchInsert(it -> {
     *      for (int i = 0; i < size; i++) {
     *          User user = new User("Frank" + i, "frank@xxx.com", "123456");
     *          user.id = i;
     *          user.updated_time = new Date();
     *          it.accept(user);
     *      }
     * }, User.class, "users");
     * </pre></blockquote>
     *
     * @param loader
     * @param clazz
     * @param table
     * @param <T>
     * @return
     * @throws UncheckedSQLException
     */
    default <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table) throws UncheckedSQLException {
        return batchInsert(loader, clazz, table, null, null);
    }

    default <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler) throws UncheckedSQLException {
        return batchInsert(loader, clazz, table, sqlHandler, null);
    }

    <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException;

    <T> BatchUpdateResult batchUpdate(String sql, int batchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String table, String columns, Consumer<Where> whereConsumer, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

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
     * @throws UncheckedSQLException
     */
    BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(PreparedStatement statement, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException;

    void batchUpdate(List<?> beans) throws UncheckedSQLException;

    /**
     * @param clazz
     * @param sqlConsumer
     * @param fun
     * @param <T>
     * @throws UncheckedSQLException
     * @since 0.2.14
     */
    default <T> void batchUpdate(Class<T> clazz, Consumer<SqlBuilder> sqlConsumer, Predicate<T> fun) throws UncheckedSQLException {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////

//    void setQueryCache(boolean b);

    void executeSqlScript(Reader reader, boolean ignoreError);

    void statement(Consumer<Statement> consumer) throws UncheckedSQLException;

    void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws UncheckedSQLException;

    void setAutoCommit(boolean autoCommit) throws UncheckedSQLException;

    boolean getAutoCommit() throws UncheckedSQLException;

    void commit() throws UncheckedSQLException;

    void rollback() throws UncheckedSQLException;

    void close() throws UncheckedSQLException;

    boolean isClosed() throws UncheckedSQLException;

    void setReadOnly(boolean readOnly) throws UncheckedSQLException;

    boolean isReadOnly() throws UncheckedSQLException;

    void setTransactionIsolation(int level) throws UncheckedSQLException;

    int getTransactionIsolation() throws UncheckedSQLException;

    SqlConnection beginTransaction() throws UncheckedSQLException;

    SqlConnection beginTransaction(int level) throws UncheckedSQLException;

    Savepoint setSavepoint() throws UncheckedSQLException;

    Savepoint setSavepoint(String name) throws UncheckedSQLException;

    void rollback(Savepoint savepoint) throws UncheckedSQLException;

    void releaseSavepoint(Savepoint savepoint) throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql, String[] columnNames) throws UncheckedSQLException;

    boolean isValid(int timeout) throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql) throws UncheckedSQLException;

    CallableStatement prepareCall(String sql) throws UncheckedSQLException;

    CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws UncheckedSQLException;

    Connection connection();
}
