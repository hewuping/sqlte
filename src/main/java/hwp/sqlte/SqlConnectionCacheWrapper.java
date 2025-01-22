package hwp.sqlte;

import hwp.sqlte.cache.Cache;

import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2019/9/12.
 */
class SqlConnectionCacheWrapper extends AbstractSqlConnection {
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
    public SqlResultSet query(String sql) throws SqlteException {
        return this.query(new SimpleSql(sql));
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws SqlteException {
        return this.query(new SimpleSql(sql, args));
    }

    @Override
    public SqlResultSet query(Sql sql) throws SqlteException {
        SqlResultSet rs = (SqlResultSet) cache.get(sql.id());
        if (rs == null) {
            rs = delegate.query(sql);
            cache.put(sql.id(), rs);
        }
        return rs;
    }

    // ---------------------------

    @Override
    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws SqlteException {
        return delegate.query(consumer);
    }

    @Override
    public void query(Sql sql, RowHandler rowHandler) throws SqlteException {
        delegate.query(sql, rowHandler);
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, RowHandler rowHandler) throws SqlteException {
        delegate.query(consumer, rowHandler);
    }

    @Override
    public void query(Sql sql, ResultSetHandler rowHandler) throws SqlteException {
        delegate.query(sql, rowHandler);
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, ResultSetHandler rowHandler) throws SqlteException {
        delegate.query(consumer, rowHandler);
    }

    @Override
    public long selectCount(Object example) throws SqlteException {
        return delegate.selectCount(example);
    }

    @Override
    public int insert(String table, String columns, Object... args) throws SqlteException {
        return delegate.insert(table, columns, args);
    }

    @Override
    public void insert(Sql sql, ResultSetHandler resultHandler) throws SqlteException {
        delegate.insert(sql, resultHandler);
    }

    @Override
    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws SqlteException {
        return delegate.insertAndReturnKey(sql, idColumn, args);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row) throws SqlteException {
        return delegate.insertMap(table, row);
    }

    @Override
    public int insertMap(String table, Consumer<Row> row) throws SqlteException {
        return delegate.insertMap(table, row);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws SqlteException {
        return delegate.insertMap(table, row, returnColumns);
    }

    @Override
    public int replaceMap(String table, Map<String, Object> row, String... returnColumns) {
        return delegate.insertMap(table, row, returnColumns);
    }

    @Override
    public int executeUpdate(String sql, Object... args) throws SqlteException {
        return delegate.executeUpdate(sql, args);
    }

    @Override
    public int update(String table, Map<String, Object> map, Where where) throws SqlteException {
        return delegate.update(table, map, where);
    }

    @Override
    public int update(String table, Map<String, Object> map, Consumer<Where> where) throws SqlteException {
        return delegate.update(table, map, where);
    }

    @Override
    public int update(String table, Consumer<Row> consumer, Consumer<Where> where) throws SqlteException {
        return delegate.update(table, consumer, where);
    }

    @Override
    public <T> T tryGet(Supplier<T> supplier, Object id) throws SqlteException {
        return delegate.tryGet(supplier, id);
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Object id) throws SqlteException {
        return delegate.tryGet(clazz, id);
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws SqlteException {
        return delegate.tryGet(clazz, consumer);
    }

    @Override
    public <T> T mustGet(Class<T> clazz, Object id) throws SqlteException {
        return delegate.mustGet(clazz, id);
    }

    @Override
    public <T, E> E loadAs(Class<T> clazz, Class<E> as, Object id) {
        return delegate.loadAs(clazz, as, id);
    }

    @Override
    public <T> T reload(T bean) throws SqlteException {
        return delegate.reload(bean);
    }

    @Override
    public <T> T reload(T bean, String table) throws SqlteException {
        return delegate.reload(bean, table);
    }

    @Override
    public void insert(Object bean) throws SqlteException {
        delegate.insert(bean);
    }

    @Override
    public void insert(Object bean, String table) throws SqlteException {
        delegate.insert(bean, table);
    }

    @Override
    public void replace(Object bean, String table) throws SqlteException {
        delegate.replace(bean, table);
    }

    @Override
    public void insertIgnore(Object bean, String table) throws SqlteException {
        delegate.insertIgnore(bean, table);
    }

    @Override
    public boolean update(Object bean) throws SqlteException {
        return delegate.update(bean);
    }

    @Override
    public boolean update(Object bean, String columns) throws SqlteException {
        return delegate.update(bean, columns);
    }

    @Override
    public boolean update(Object bean, boolean ignoreNullValues) throws SqlteException {
        return delegate.update(bean, ignoreNullValues);
    }

    @Override
    public boolean update(Object bean, UpdateOptions options) throws SqlteException {
        return delegate.update(bean, options);
    }

    @Override
    public boolean delete(Object bean) throws SqlteException {
        return delegate.delete(bean);
    }

    @Override
    public boolean delete(Object bean, String table) throws SqlteException {
        return delegate.delete(bean, table);
    }

    @Override
    public int delete(String table, Consumer<Where> whereConsumer) throws SqlteException {
        return delegate.delete(table, whereConsumer);
    }

    @Override
    public <T> BatchUpdateResult batchDelete(List<T> beans, String table) throws SqlteException {
        return delegate.batchDelete(beans, table);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws SqlteException {
        return delegate.batchUpdate(sql, it, consumer);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(List<T> beans, String table) throws SqlteException {
        return delegate.batchInsert(beans, table);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(List<T> beans, String table, SqlHandler sqlHandler) throws SqlteException {
        return delegate.batchInsert(beans, table, sqlHandler);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(DataLoader<T> loader, Class<T> clazz, String table) throws SqlteException {
        return delegate.batchInsert(loader, clazz, table);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(DataLoader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler) throws SqlteException {
        return delegate.batchInsert(loader, clazz, table, sqlHandler);
    }

    @Override
    public <T> BatchUpdateResult batchInsert(DataLoader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler, BiConsumer<PreparedStatement, int[]> psConsumer) throws SqlteException {
        return delegate.batchInsert(loader, clazz, table, sqlHandler, psConsumer);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, int batchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws SqlteException {
        return delegate.batchUpdate(sql, batchSize, it, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws SqlteException {
        return delegate.batchUpdate(sql, consumer);
    }

    @Override
    public BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws SqlteException {
        return delegate.batchInsert(table, columns, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer) throws SqlteException {
        return delegate.batchUpdate(sql, batchSize, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws SqlteException {
        return delegate.batchUpdate(sql, batchSize, consumer, psConsumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(PreparedStatement statement, int batchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws SqlteException {
        return delegate.batchUpdate(statement, batchSize, consumer, psConsumer);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(List<T> beans, String table, String columns) throws SqlteException {
        return delegate.batchUpdate(beans, table, columns);
    }

    @Override
    public void executeSqlScript(Reader reader, boolean ignoreError) {
        delegate.executeSqlScript(reader, ignoreError);
    }

    @Override
    public void statement(Consumer<Statement> consumer) throws SqlteException {
        delegate.statement(consumer);
    }

    @Override
    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws SqlteException {
        delegate.prepareStatement(sql, consumer);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SqlteException {
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SqlteException {
        return delegate.getAutoCommit();
    }

    @Override
    public void commit() throws SqlteException {
        delegate.commit();
    }

    @Override
    public void rollback() throws SqlteException {
        delegate.rollback();
    }

    @Override
    public void close() throws SqlteException {
        delegate.close();
    }

    @Override
    public boolean isClosed() throws SqlteException {
        return delegate.isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SqlteException {
        delegate.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SqlteException {
        return delegate.isReadOnly();
    }

    @Override
    public void setTransactionIsolation(int level) throws SqlteException {
        delegate.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SqlteException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public SqlConnection beginTransaction() throws SqlteException {
        return delegate.beginTransaction();
    }

    @Override
    public SqlConnection beginTransaction(int level) throws SqlteException {
        return delegate.beginTransaction(level);
    }

    @Override
    public Savepoint setSavepoint() throws SqlteException {
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SqlteException {
        return delegate.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SqlteException {
        delegate.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SqlteException {
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SqlteException {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SqlteException {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SqlteException {
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public boolean isValid(int timeout) throws SqlteException {
        return delegate.isValid(timeout);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SqlteException {
        return delegate.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SqlteException {
        return delegate.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SqlteException {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Connection connection() {
        return delegate.connection();
    }


}
