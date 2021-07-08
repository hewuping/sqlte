package hwp.sqlte;

import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2018/10/31.
 */
public interface SqlConnection extends AutoCloseable {

    //    Query query() throws UncheckedSQLException;
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

    default <T> DataList<T> query(Supplier<T> supplier, Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        String sql = sb.sql();
        int form = sql.lastIndexOf("LIMIT ");
        if (form == -1) {
            throw new IllegalArgumentException("Limit clause not found: " + sql);
        }
        List<T> list = query(sql, sb.args()).list(supplier);
        String countSql = "SELECT COUNT(*) FROM (" + sql.substring(0, form) + ") AS _t";
        Long count = query(countSql, sb.args()).firstLong();
        return new DataList<>(list, count);
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
        return query(sql -> sql.selectCount(table).where(where)).firstLong();
    }

    default boolean selectExists(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder sql = new SqlBuilder();
        consumer.accept(sql);
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT EXISTS(");
        builder.append(sql.sql());
        builder.append(")");
        return query(builder.toString(), sql.args()).firstLong() == 1;
    }

/*    default <T> List<T> queryList(Supplier<T> supplier, Consumer<SqlBuilder> consumer) {
        return this.query(consumer).list(supplier);
    }*/

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

    <T> T load(Supplier<T> supplier, Object id) throws UncheckedSQLException;

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

    BatchUpdateResult batchInsert(List<?> beans, String table) throws UncheckedSQLException;

    BatchUpdateResult batchInsert(List<?> beans, String table, Function<String, String> sqlProcessor) throws UncheckedSQLException;

    default <T> BatchUpdateResult batchInsert(Consumer<Consumer<T>> consumer, Class<T> clazz, String table) throws UncheckedSQLException {
        return batchInsert(consumer, clazz, table, null, null);
    }

    default <T> BatchUpdateResult batchInsert(Consumer<Consumer<T>> consumer, Class<T> clazz, String table, Function<String, String> sqlProcessor) throws UncheckedSQLException {
        return batchInsert(consumer, clazz, table, sqlProcessor, null);
    }

    <T> BatchUpdateResult batchInsert(Consumer<Consumer<T>> consumer, Class<T> clazz, String table, Function<String, String> sqlProcessor, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException;

    <T> BatchUpdateResult batchUpdate(String sql, int batchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String table, String columns, Consumer<Where> whereConsumer, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(PreparedStatement statement, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException;

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
