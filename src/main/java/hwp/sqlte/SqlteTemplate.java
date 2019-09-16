package hwp.sqlte;

import javax.sql.DataSource;
import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <pre>
 *     for Spring:
 *     @Bean
 *     public SqlteTemplate getSqlteTemplate(DataSource dataSource) {
 *         Sql.config().setDataSource(dataSource)
 *         SqlteTemplate template = new SqlteTemplate() {
 *             @Override
 *             protected Connection open(DataSource dataSource) {
 *                 return DataSourceUtils.getConnection(dataSource);
 *             }
 *
 *             @Override
 *             protected void close(Connection connection) {
 *                 DataSourceUtils.releaseConnection(connection, dataSource);
 *             }
 *         };
 *         return template;
 *     }
 * </pre>
 *
 * @author Zero
 * Created on 2018/11/5.
 */
public class SqlteTemplate implements SqlConnection {//sql

    private boolean cacheable;

    @Override
    public SqlteTemplate cacheable() {
        this.cacheable = true;
        return this;
    }

    @Override
    public void executeSqlScript(Reader reader, boolean ignoreError) {
        run(conn -> {
            conn.executeSqlScript(reader, ignoreError);
            return null;
        });
    }

    @Override
    public void statement(Consumer<Statement> consumer) throws UncheckedSQLException {
        run(conn -> {
            conn.statement(consumer);
            return null;
        });
    }

    @Override
    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws UncheckedSQLException {
        run(conn -> {
            conn.prepareStatement(sql, consumer);
            return null;
        });
    }

    @Override
    public SqlResultSet query(String sql) throws UncheckedSQLException {
        return run(conn -> conn.query(sql));
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws UncheckedSQLException {
        return run(conn -> conn.query(sql, args));
    }

    @Override
    public SqlResultSet query(Sql sql) throws UncheckedSQLException {
        return run(conn -> conn.query(sql));
    }

    @Override
    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        return run(conn -> conn.query(consumer));
    }

    @Override
    public <T> T load(Supplier<T> supplier, Object id) throws UncheckedSQLException {
        return run(conn -> conn.load(supplier, id));
    }

    @Override
    public <T> T reload(T bean) throws UncheckedSQLException {
        return run(conn -> conn.reload(bean));
    }

    @Override
    public void query(Sql sql, Consumer<ResultSet> rowHandler) throws UncheckedSQLException {
        run(conn -> {
            conn.query(sql, rowHandler);
            return null;
        });
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, Consumer<ResultSet> rowHandler) throws UncheckedSQLException {
        run(conn -> {
            conn.query(consumer, rowHandler);
            return null;
        });
    }

    @Override
    public void query(String sql, Consumer<ResultSet> rowHandler, Object... args) throws UncheckedSQLException {
        run(conn -> {
            conn.query(sql, rowHandler, args);
            return null;
        });
    }

    @Override
    public void query(Sql sql, RowHandler rowHandler) throws UncheckedSQLException {
        run(conn -> {
            conn.query(sql, rowHandler);
            return null;
        });
    }


    @Override
    public int insert(String table, String columns, Object... args) throws UncheckedSQLException {
        return run(conn -> conn.insert(table, columns, args));
    }

    @Override
    public void insert(Sql sql, Consumer<ResultSet> resultHandler) throws UncheckedSQLException {
        run(conn -> {
            conn.insert(sql, resultHandler);
            return null;
        });
    }

    @Override
    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws UncheckedSQLException {
        return run(conn -> conn.insertAndReturnKey(sql, idColumn, args));
    }

    @Override
    public void insert(Object bean, String table) throws UncheckedSQLException {
        run(conn -> {
            conn.insert(bean, table);
            return null;
        });
    }

    @Override
    public boolean update(Object bean) throws UncheckedSQLException {
        return this.update(bean, null);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row) throws UncheckedSQLException {
        return this.insertMap(table, row, (String[]) null);
    }

    @Override
    public int insertMap(String table, Consumer<Row> row) throws UncheckedSQLException {
        Row _map = new Row();
        row.accept(_map);
        return this.insertMap(table, _map);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws UncheckedSQLException {
        return run(conn -> conn.insertMap(table, row, returnColumns));
    }

    @Override
    public int executeUpdate(String sql, Object... args) throws UncheckedSQLException {
        return run(conn -> conn.executeUpdate(sql, args));
    }

    @Override
    public int update(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        return run(conn -> conn.update(consumer));
    }

    @Override
    public boolean update(Object bean, String columns) throws UncheckedSQLException {
        return run(conn -> conn.update(bean, columns));
    }

    @Override
    public <T> void batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        run(conn -> {
            conn.batchUpdate(sql, it, consumer);
            return null;
        });
    }

    @Override
    public BatchUpdateResult batchInsert(List<?> beans, String table) throws UncheckedSQLException {
        return run(conn -> conn.batchInsert(beans, table));
    }

    @Override
    public BatchUpdateResult batchInsert(List<?> beans, String table, Function<String, String> sqlProcessor) throws UncheckedSQLException {
        return run(conn -> conn.batchInsert(beans, table, sqlProcessor));
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Consumer<Consumer<T>> consumer, Class<T> clazz, String table) throws UncheckedSQLException {
        return run(conn -> conn.batchInsert(consumer, clazz, table, null));
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Consumer<Consumer<T>> consumer, Class<T> clazz, String table, Function<String, String> sqlProcessor) throws UncheckedSQLException {
        return run(conn -> conn.batchInsert(consumer, clazz, table, sqlProcessor));
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Consumer<Consumer<T>> consumer, Class<T> clazz, String table, Function<String, String> sqlProcessor, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        return run(conn -> conn.batchInsert(consumer, clazz, table, sqlProcessor, psConsumer));
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        return run(conn -> conn.batchUpdate(sql, maxBatchSize, it, consumer));
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return run(conn -> conn.batchUpdate(sql, consumer));
    }

    @Override
    public BatchUpdateResult batchUpdate(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return run(conn -> conn.batchUpdate(table, columns, consumer));
    }

    @Override
    public BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return run(conn -> conn.batchInsert(table, columns, consumer));
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return run(conn -> conn.batchUpdate(sql, maxBatchSize, consumer));
    }

    @Override
    public BatchUpdateResult batchUpdate(PreparedStatement statement, int maxBatchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        return run(conn -> conn.batchUpdate(statement, maxBatchSize, consumer, psConsumer));
    }

    @Override
    public boolean update(Object bean, String table, String columns, boolean ignoreNullValue, Consumer<Where> where) throws UncheckedSQLException {
        return run(conn -> conn.update(bean, table, columns, ignoreNullValue, where));
    }

    @Override
    public boolean delete(Object bean, String table) throws UncheckedSQLException {
        return run(conn -> conn.delete(bean, table));
    }

    @Override
    public int update(String table, Map<String, Object> map, Where where) throws UncheckedSQLException {
        return run(conn -> conn.update(table, map, where));
    }

    @Override
    public int updateByPks(String table, Map<String, Object> map, String... pk) throws UncheckedSQLException {
        return run(conn -> conn.updateByPks(table, map, pk));
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws UncheckedSQLException {
        run(conn -> {
            conn.setAutoCommit(autoCommit);
            return null;
        });
    }

    @Override
    public boolean getAutoCommit() throws UncheckedSQLException {
        return run(SqlConnection::getAutoCommit);
    }

    @Override
    public void commit() throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual commit is not allowed");
    }

    @Override
    public void rollback() throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual rollback is not allowed");
    }

    @Override
    public void close() throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual close is not allowed");
    }

    @Override
    public boolean isClosed() throws UncheckedSQLException {
        return run(SqlConnection::isClosed);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws UncheckedSQLException {
        run(conn -> {
            conn.setReadOnly(readOnly);
            return null;
        });
    }

    @Override
    public boolean isReadOnly() throws UncheckedSQLException {
        return run(SqlConnection::isReadOnly);
    }

    @Override
    public void setTransactionIsolation(int level) throws UncheckedSQLException {
        run(conn -> {
            conn.setTransactionIsolation(level);
            return null;
        });
    }

    @Override
    public int getTransactionIsolation() throws UncheckedSQLException {
        return run(SqlConnection::getTransactionIsolation);
    }

    @Override
    public SqlConnection beginTransaction() throws UncheckedSQLException {
        return run(SqlConnection::beginTransaction);
    }

    @Override
    public SqlConnection beginTransaction(int level) throws UncheckedSQLException {
        return run(conn -> conn.beginTransaction(level));
    }

    @Override
    public Savepoint setSavepoint() throws UncheckedSQLException {
        return run(SqlConnection::setSavepoint);
    }

    @Override
    public Savepoint setSavepoint(String name) throws UncheckedSQLException {
        return run(conn -> conn.setSavepoint(name));
    }

    @Override
    public void rollback(Savepoint savepoint) throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual rollback is not allowed");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws UncheckedSQLException {
        run(connection -> {
            connection.releaseSavepoint(savepoint);
            return null;
        });
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws UncheckedSQLException {
        return run(conn -> prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws UncheckedSQLException {
        return run(conn -> conn.prepareStatement(sql, autoGeneratedKeys));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws UncheckedSQLException {
        return run(conn -> conn.prepareStatement(sql, columnNames));
    }

    @Override
    public boolean isValid(int timeout) throws UncheckedSQLException {
        return run(conn -> conn.isValid(timeout));
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws UncheckedSQLException {
        return run(conn -> conn.prepareStatement(sql));
    }

    @Override
    public CallableStatement prepareCall(String sql) throws UncheckedSQLException {
        return run(conn -> conn.prepareCall(sql));
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws UncheckedSQLException {
        return run(conn -> conn.prepareCall(sql, resultSetType, resultSetConcurrency));
    }

    @Override
    public Connection connection() {
        return null;
    }


    protected Connection open(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    protected void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            //Ignore
        }
    }

    private <R> R run(Function<SqlConnection, R> function) {
        Connection conn = open(Sql.config().getDataSource());
        try {
            SqlConnection sqlConn = SqlConnectionImpl.use(conn);
            return function.apply(cacheable ? sqlConn.cacheable() : sqlConn);
        } finally {
            close(conn);
        }
    }

}
