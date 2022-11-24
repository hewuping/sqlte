package hwp.sqlte;

import hwp.sqlte.cache.Cache;

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
 * Created on 2019/9/12.
 */
class SqlConnectionCacheWrapper implements SqlConnection {
    private Cache cache;
    private SqlConnection delegate;

    public SqlConnectionCacheWrapper(SqlConnection delegate, Cache cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public SqlConnection cacheable() {
        return this;
    }

    @Override
    public SqlResultSet query(String sql) throws UncheckedSQLException {
        return this.query(new SimpleSql(sql));
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws UncheckedSQLException {
        return this.query(new SimpleSql(sql, args));
    }

    @Override
    public SqlResultSet query(Sql sql) throws UncheckedSQLException {
        SqlResultSet rs = (SqlResultSet) cache.get(sql.id());
        if (rs == null) {
            rs = delegate.query(sql);
            cache.put(sql.id(), rs);
        }
        return rs;
    }

    // ---------------------------

    @Override
    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        return delegate.query(consumer);
    }

    @Override
    public void query(Sql sql, RowHandler rowHandler) throws UncheckedSQLException {
        delegate.query(sql, rowHandler);
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, RowHandler rowHandler) throws UncheckedSQLException {
        delegate.query(consumer, rowHandler);
    }

    @Override
    public void query(Sql sql, ResultSetHandler rowHandler) throws UncheckedSQLException {
        delegate.query(sql, rowHandler);
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, ResultSetHandler rowHandler) throws UncheckedSQLException {
        delegate.query(consumer, rowHandler);
    }

    @Override
    public int insert(String table, String columns, Object... args) throws UncheckedSQLException {
        return delegate.insert(table, columns, args);
    }

    @Override
    public void insert(Sql sql, ResultSetHandler resultHandler) throws UncheckedSQLException {
        delegate.insert(sql, resultHandler);
    }

    @Override
    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws UncheckedSQLException {
        return delegate.insertAndReturnKey(sql, idColumn, args);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row) throws UncheckedSQLException {
        return delegate.insertMap(table, row);
    }

    @Override
    public int insertMap(String table, Consumer<Row> row) throws UncheckedSQLException {
        return delegate.insertMap(table, row);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws UncheckedSQLException {
        return delegate.insertMap(table, row, returnColumns);
    }

    @Override
    public int replaceMap(String table, Map<String, Object> row, String... returnColumns) {
        return delegate.insertMap(table, row, returnColumns);
    }

    @Override
    public int insertIgnoreMap(String table, Map<String, Object> row, String... returnColumns) {
        return delegate.insertIgnoreMap(table, row, returnColumns);
    }

    @Override
    public int executeUpdate(String sql, Object... args) throws UncheckedSQLException {
        return delegate.executeUpdate(sql, args);
    }

    @Override
    public int update(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        return delegate.update(consumer);
    }

    @Override
    public int update(String table, Map<String, Object> map, Where where) throws UncheckedSQLException {
        return delegate.update(table, map, where);
    }

    @Override
    public int update(String table, Map<String, Object> map, Consumer<Where> where) throws UncheckedSQLException {
        return delegate.update(table, map, where);
    }

    @Override
    public int update(String table, Consumer<Row> consumer, Consumer<Where> where) throws UncheckedSQLException {
        return delegate.update(table, consumer, where);
    }

    @Override
    public int updateByPks(String table, Map<String, Object> map, String... pk) throws UncheckedSQLException {
        return delegate.updateByPks(table, map, pk);
    }

    @Override
    public <T> T tryGet(Supplier<T> supplier, Object id) throws UncheckedSQLException {
        return delegate.tryGet(supplier, id);
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Object id) throws UncheckedSQLException {
        return delegate.tryGet(clazz, id);
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws UncheckedSQLException {
        return delegate.tryGet(clazz, consumer);
    }

    @Override
    public <T> T mustGet(Class<T> clazz, Object id) throws UncheckedSQLException {
        return delegate.mustGet(clazz, id);
    }

    @Override
    public <T> T reload(T bean) throws UncheckedSQLException {
        return delegate.reload(bean);
    }

    @Override
    public void insert(Object bean) throws UncheckedSQLException {
        delegate.insert(bean);
    }

    @Override
    public void insert(Object bean, String table) throws UncheckedSQLException {
        delegate.insert(bean, table);
    }

    @Override
    public void replace(Object bean, String table) throws UncheckedSQLException {
        delegate.replace(bean, table);
    }

    @Override
    public void insertIgnore(Object bean, String table) throws UncheckedSQLException {
        delegate.insertIgnore(bean, table);
    }

    @Override
    public boolean update(Object bean, String table, String columns, boolean ignoreNullValue, Consumer<Where> where) throws UncheckedSQLException {
        return delegate.update(bean, table, columns, ignoreNullValue, where);
    }

    @Override
    public boolean update(Object bean, String table, Consumer<Where> where) throws UncheckedSQLException {
        return delegate.update(bean, table, where);
    }

    @Override
    public boolean update(Object bean, String table, String columns, boolean ignoreNullValue) throws UncheckedSQLException {
        return delegate.update(bean, table, columns, ignoreNullValue);
    }

    @Override
    public boolean update(Object bean, String columns, boolean ignoreNullValue) throws UncheckedSQLException {
        return delegate.update(bean, columns, ignoreNullValue);
    }

    @Override
    public boolean update(Object bean, String columns) throws UncheckedSQLException {
        return delegate.update(bean, columns);
    }

    @Override
    public boolean update(Object bean, boolean ignoreNullValue) throws UncheckedSQLException {
        return delegate.update(bean, ignoreNullValue);
    }

    @Override
    public boolean update(Object bean) throws UncheckedSQLException {
        return delegate.update(bean);
    }

    @Override
    public boolean delete(Object bean) throws UncheckedSQLException {
        return delegate.delete(bean);
    }

    @Override
    public boolean delete(Object bean, String table) throws UncheckedSQLException {
        return delegate.delete(bean, table);
    }

    @Override
    public int delete(String table, Consumer<Where> whereConsumer) throws UncheckedSQLException {
        return delegate.delete(table, whereConsumer);
    }

    @Override
    public <T> BatchUpdateResult batchDelete(List<T> beans, String table) throws UncheckedSQLException {
        return delegate.batchDelete(beans, table);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        return delegate.batchUpdate(sql, it, consumer);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(List<T> beans, String table) throws UncheckedSQLException {
        return delegate.batchInsert(beans, table);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(List<T> beans, String table, SqlHandler sqlHandler) throws UncheckedSQLException {
        return delegate.batchInsert(beans, table, sqlHandler);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table) throws UncheckedSQLException {
        return delegate.batchInsert(loader, clazz, table);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler) throws UncheckedSQLException {
        return delegate.batchInsert(loader, clazz, table, sqlHandler);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Loader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        return delegate.batchInsert(loader, clazz, table, sqlHandler, psConsumer);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, int batchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        return delegate.batchUpdate(sql, batchSize, it, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return delegate.batchUpdate(sql, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String table, String columns, Consumer<Where> whereConsumer, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return delegate.batchUpdate(table, columns, whereConsumer, consumer);
    }

    @Override
    public BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return delegate.batchInsert(table, columns, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return delegate.batchUpdate(sql, batchSize, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        return delegate.batchUpdate(sql, batchSize, consumer, psConsumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(PreparedStatement statement, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        return delegate.batchUpdate(statement, batchSize, consumer, psConsumer);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(List<T> beans, String table) throws UncheckedSQLException {
        return delegate.batchUpdate(beans, table);
    }

    @Override
    public void executeSqlScript(Reader reader, boolean ignoreError) {
        delegate.executeSqlScript(reader, ignoreError);
    }

    @Override
    public void statement(Consumer<Statement> consumer) throws UncheckedSQLException {
        delegate.statement(consumer);
    }

    @Override
    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws UncheckedSQLException {
        delegate.prepareStatement(sql, consumer);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws UncheckedSQLException {
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws UncheckedSQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public void commit() throws UncheckedSQLException {
        delegate.commit();
    }

    @Override
    public void rollback() throws UncheckedSQLException {
        delegate.rollback();
    }

    @Override
    public void close() throws UncheckedSQLException {
        delegate.close();
    }

    @Override
    public boolean isClosed() throws UncheckedSQLException {
        return delegate.isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws UncheckedSQLException {
        delegate.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws UncheckedSQLException {
        return delegate.isReadOnly();
    }

    @Override
    public void setTransactionIsolation(int level) throws UncheckedSQLException {
        delegate.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws UncheckedSQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public SqlConnection beginTransaction() throws UncheckedSQLException {
        return delegate.beginTransaction();
    }

    @Override
    public SqlConnection beginTransaction(int level) throws UncheckedSQLException {
        return delegate.beginTransaction(level);
    }

    @Override
    public Savepoint setSavepoint() throws UncheckedSQLException {
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws UncheckedSQLException {
        return delegate.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws UncheckedSQLException {
        delegate.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws UncheckedSQLException {
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws UncheckedSQLException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws UncheckedSQLException {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws UncheckedSQLException {
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public boolean isValid(int timeout) throws UncheckedSQLException {
        return delegate.isValid(timeout);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws UncheckedSQLException {
        return delegate.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws UncheckedSQLException {
        return delegate.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws UncheckedSQLException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Connection connection() {
        return delegate.connection();
    }


}
