package hwp.sqlte;

import javax.sql.DataSource;
import java.io.Reader;
import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * for Spring:
 *
 *
 *
 * <pre>{@code
 * @Bean
 * public SqlteTemplate getSqlteTemplate(DataSource dataSource) {
 *     Sql.config().setDataSource(dataSource)
 *     SqlteTemplate template = new SqlteTemplate() {
 *        @Override
 *        protected Connection open(DataSource dataSource) {
 *            return DataSourceUtils.getConnection(dataSource);
 *        }
 *
 *        @Override
 *         protected void close(Connection connection) {
 *            DataSourceUtils.releaseConnection(connection, dataSource);
 *        }
 *    };
 *    return template;
 * }
 * } </pre>
 *
 * @author Zero
 * Created on 2018/11/5.
 */
public class SqlteTemplate extends AbstractSqlConnection {//sql

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
    public void statement(Consumer<Statement> consumer) throws SqlteException {
        run(conn -> {
            conn.statement(consumer);
            return null;
        });
    }

    @Override
    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws SqlteException {
        run(conn -> {
            conn.prepareStatement(sql, consumer);
            return null;
        });
    }

    @Override
    public SqlResultSet query(String sql) throws SqlteException {
        return run(conn -> conn.query(sql));
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws SqlteException {
        return run(conn -> conn.query(sql, args));
    }

    @Override
    public SqlResultSet query(Sql sql) throws SqlteException {
        return run(conn -> conn.query(sql));
    }

    @Override
    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws SqlteException {
        return run(conn -> conn.query(consumer));
    }

    @Override
    public <T> Page<T> queryPage(Consumer<SqlBuilder> consumer, Supplier<T> supplier) throws SqlteException {
        return run(conn -> conn.queryPage(consumer, supplier));
    }

    @Override
    public <T> T tryGet(Supplier<T> supplier, Object id) throws SqlteException {
        return run(conn -> conn.tryGet(supplier, id));
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Object id) throws SqlteException {
        return run(conn -> conn.tryGet(clazz, id));
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws SqlteException {
        return run(conn -> conn.tryGet(clazz, consumer));
    }

    @Override
    public <T> T mustGet(Class<T> clazz, Object id) throws SqlteException {
        return run(conn -> conn.mustGet(clazz, id));
    }

    @Override
    public <T, E> E loadAs(Class<T> clazz, Class<E> as, Object id) {
        return run(conn -> conn.loadAs(clazz, as, id));
    }

    @Override
    public <T> T reload(T bean) throws SqlteException {
        return run(conn -> conn.reload(bean));
    }

    @Override
    public <T> T reload(T bean, String table) throws SqlteException {
        return run(conn -> conn.reload(bean, table));
    }

    @Override
    public void query(Sql sql, ResultSetHandler rowHandler) throws SqlteException {
        run(conn -> {
            conn.query(sql, rowHandler);
            return null;
        });
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, ResultSetHandler rowHandler) throws SqlteException {
        run(conn -> {
            conn.query(consumer, rowHandler);
            return null;
        });
    }

    @Override
    public long selectCount(Object example) throws SqlteException {
        return run(conn -> conn.selectCount(example));
    }


    @Override
    public void query(Sql sql, RowHandler rowHandler) throws SqlteException {
        run(conn -> {
            conn.query(sql, rowHandler);
            return null;
        });
    }


    @Override
    public int insert(String table, String columns, Object... args) throws SqlteException {
        return run(conn -> conn.insert(table, columns, args));
    }

    @Override
    public void insert(Sql sql, ResultSetHandler resultHandler) throws SqlteException {
        run(conn -> {
            conn.insert(sql, resultHandler);
            return null;
        });
    }

    @Override
    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws SqlteException {
        return run(conn -> conn.insertAndReturnKey(sql, idColumn, args));
    }

    @Override
    public void insert(Object bean, String table) throws SqlteException {
        run(conn -> {
            conn.insert(bean, table);
            return null;
        });
    }

    @Override
    public void replace(Object bean, String table) throws SqlteException {
        run(conn -> {
            conn.replace(bean, table);
            return null;
        });
    }


    @Override
    public int insertMap(String table, Map<String, Object> row) throws SqlteException {
        return this.insertMap(table, row, (String[]) null);
    }

    @Override
    public int insertMap(String table, Consumer<Row> row) throws SqlteException {
        Row _map = new Row();
        row.accept(_map);
        return this.insertMap(table, _map);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws SqlteException {
        return run(conn -> conn.insertMap(table, row, returnColumns));
    }

    @Override
    public int replaceMap(String table, Map<String, Object> row, String... returnColumns) {
        return run(conn -> conn.replaceMap(table, row, returnColumns));
    }

    @Override
    public int executeUpdate(String sql, Object... args) throws SqlteException {
        return run(conn -> conn.executeUpdate(sql, args));
    }

    @Override
    public boolean update(Object bean, UpdateOptions options) throws SqlteException {
        return run(conn -> conn.update(bean, options));
    }


    @Override
    public <T> BatchUpdateResult batchInsert(Collection<T> beans, UpdateOptions options) throws SqlteException {
        return run(conn -> conn.batchInsert(beans, options));
    }

    @Override
    public <T> BatchUpdateResult batchInsert(Class<T> clazz, DataLoader<T> loader, UpdateOptions options) throws SqlteException {
        return run(conn -> conn.batchInsert(clazz, loader, options));
    }


    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws SqlteException {
        return run(conn -> conn.batchUpdate(sql, consumer));
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer, UpdateOptions options) throws SqlteException {
        return run(conn -> conn.batchUpdate(sql, consumer, options));
    }


    @Override
    public <T> BatchUpdateResult batchUpdate(Collection<T> beans, UpdateOptions options) throws SqlteException {
        return run(conn -> {
            return conn.batchUpdate(beans, options);
        });
    }

    @Override
    public <T> boolean delete(T bean, String table) throws SqlteException {
        return run(conn -> conn.delete(bean, table));
    }

    @Override
    public <T> int deleteAll(Collection<T> beans, String table) throws SqlteException {
        return run(conn -> {
            return conn.deleteAll(beans, table);
        });
    }

    @Override
    public int update(String table, Map<String, Object> data, Where where) throws SqlteException {
        return run(conn -> conn.update(table, data, where));
    }


    @Override
    public void setAutoCommit(boolean autoCommit) throws SqlteException {
        run(conn -> {
            conn.setAutoCommit(autoCommit);
            return null;
        });
    }

    @Override
    public boolean getAutoCommit() throws SqlteException {
        return run(SqlConnection::getAutoCommit);
    }

    @Override
    public void commit() throws SqlteException {
        throw new UnsupportedOperationException("Manual commit is not allowed");
    }

    @Override
    public void rollback() throws SqlteException {
        throw new UnsupportedOperationException("Manual rollback is not allowed");
    }

    @Override
    public void close() throws SqlteException {
        // 这里不再抛异常, Spring 销毁Bean时, 会调用 close() 等方法, 这里直接忽略就行
        // throw new UnsupportedOperationException("Manual close is not allowed");
    }

    @Override
    public boolean isClosed() throws SqlteException {
        return run(SqlConnection::isClosed);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SqlteException {
        run(conn -> {
            conn.setReadOnly(readOnly);
            return null;
        });
    }

    @Override
    public boolean isReadOnly() throws SqlteException {
        return run(SqlConnection::isReadOnly);
    }

    @Override
    public void setTransactionIsolation(int level) throws SqlteException {
        run(conn -> {
            conn.setTransactionIsolation(level);
            return null;
        });
    }

    @Override
    public int getTransactionIsolation() throws SqlteException {
        return run(SqlConnection::getTransactionIsolation);
    }

    @Override
    public SqlConnection beginTransaction() throws SqlteException {
        return run(SqlConnection::beginTransaction);
    }

    @Override
    public SqlConnection beginTransaction(int level) throws SqlteException {
        return run(conn -> conn.beginTransaction(level));
    }

    @Override
    public Savepoint setSavepoint() throws SqlteException {
        return run(SqlConnection::setSavepoint);
    }

    @Override
    public Savepoint setSavepoint(String name) throws SqlteException {
        return run(conn -> conn.setSavepoint(name));
    }

    @Override
    public void rollback(Savepoint savepoint) throws SqlteException {
        throw new UnsupportedOperationException("Manual rollback is not allowed");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SqlteException {
        run(connection -> {
            connection.releaseSavepoint(savepoint);
            return null;
        });
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SqlteException {
        return run(conn -> prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SqlteException {
        return run(conn -> conn.prepareStatement(sql, autoGeneratedKeys));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SqlteException {
        return run(conn -> conn.prepareStatement(sql, columnNames));
    }

    @Override
    public boolean isValid(int timeout) throws SqlteException {
        return run(conn -> conn.isValid(timeout));
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SqlteException {
        return run(conn -> conn.prepareStatement(sql));
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SqlteException {
        return run(conn -> conn.prepareCall(sql));
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SqlteException {
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
            throw new SqlteException(e);
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
