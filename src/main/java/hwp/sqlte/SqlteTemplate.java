package hwp.sqlte;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.Reader;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * <pre>
 *     @Bean
 *     public SqlteTemplate getSqlteTemplate(DataSource dataSource) {
 *         Sql.config().setDataSource(dataSource);
 *         return new SqlteTemplate(dataSource);
 *     }
 * </pre>
 *
 * @author Zero
 * Created on 2018/11/5.
 */
public class SqlteTemplate implements SqlConnection {//sql

    private DataSource dataSource;

    public SqlteTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void executeSqlScript(Reader reader, boolean ignoreError) {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.executeSqlScript(reader, ignoreError);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void statement(Consumer<Statement> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.statement(consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.prepareStatement(sql, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.query(sql, args);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public SqlResultSet query(Sql sql) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.query(sql);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.query(consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void query(Sql sql, Consumer<ResultSet> rowHandler) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.query(sql, rowHandler);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void query(Consumer<SqlBuilder> consumer, Consumer<ResultSet> rowHandler) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.query(consumer, rowHandler);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void query(String sql, Consumer<ResultSet> rowHandler, Object... args) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.query(sql, rowHandler, args);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void query(Sql sql, RowHandler rowHandler) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.query(sql, rowHandler);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int insert(Sql sql) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.insert(sql);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int insert(String sql, Object... args) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.insert(sql, args);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int insert(String table, String columns, Object... args) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.insert(table, columns, args);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void insert(Sql sql, Consumer<ResultSet> resultHandler) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.insert(sql, resultHandler);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.insertAndReturnKey(sql, idColumn, args);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void insertBean(Object bean) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.insertBean(bean);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void insertBean(Object bean, String table) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.insertBean(bean, table);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void insertBean(Object bean, String table, String... returnColumns) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.insertBean(bean, table, returnColumns);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int insertMap(String table, Map<String, Object> row) throws UncheckedSQLException {
        return this.insertMap(table, row, (String[]) null);
    }

    @Override
    public int insertMap(String table, Consumer<Map<String, Object>> map) throws UncheckedSQLException {
        Map<String, Object> _map = new HashMap<>();
        map.accept(_map);
        return this.insertMap(table, _map);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.insertMap(table, row, returnColumns);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int update(String sql, Object... args) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.update(sql, args);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int update(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.update(consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public <T> void batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.batchUpdate(sql, it, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void batchInsert(List<?> beans, String table) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            conn.batchInsert(beans, table);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.batchUpdate(sql, maxBatchSize, it, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.batchUpdate(sql, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.batchUpdate(table, columns, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.batchInsert(table, columns, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.batchUpdate(sql, maxBatchSize, consumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(PreparedStatement statement, int maxBatchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.batchUpdate(statement, maxBatchSize, consumer, psConsumer);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int update(Object bean, String table, Consumer<Where> where) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.update(bean, table, where);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int update(Map<String, Object> map, String table, Where where) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.update(map, table, where);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int update(Map<String, Object> map, String table, Consumer<Where> where) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.update(map, table, where);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public int updateByPks(Map<String, Object> map, String table, String... pk) throws UncheckedSQLException {
        SqlConnection conn = null;
        try {
            conn = getSqlConnection();
            return conn.updateByPks(map, table, pk);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn.connection(), dataSource);
            }
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws UncheckedSQLException {
        getSqlConnection().setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws UncheckedSQLException {
        return getSqlConnection().getAutoCommit();
    }

    @Override
    public void commit() throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlConnection");
    }

    @Override
    public void rollback() throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlConnection");
    }

    @Override
    public void close() throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlConnection");
    }

    @Override
    public boolean isClosed() throws UncheckedSQLException {
        return getSqlConnection().isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws UncheckedSQLException {
        getSqlConnection().setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws UncheckedSQLException {
        return getSqlConnection().isReadOnly();
    }

    @Override
    public void setTransactionIsolation(int level) throws UncheckedSQLException {
        getSqlConnection().setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws UncheckedSQLException {
        return getSqlConnection().getTransactionIsolation();
    }

    @Override
    public SqlConnection beginTransaction() throws UncheckedSQLException {
        return getSqlConnection().beginTransaction();
    }

    @Override
    public SqlConnection beginTransaction(int level) throws UncheckedSQLException {
        return getSqlConnection().beginTransaction(level);
    }

    @Override
    public Savepoint setSavepoint() throws UncheckedSQLException {
        return getSqlConnection().setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws UncheckedSQLException {
        return getSqlConnection().setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws UncheckedSQLException {
        throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlConnection");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws UncheckedSQLException {
        getSqlConnection().releaseSavepoint(savepoint);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws UncheckedSQLException {
        return getSqlConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws UncheckedSQLException {
        return getSqlConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws UncheckedSQLException {
        return getSqlConnection().prepareStatement(sql, columnNames);
    }

    @Override
    public boolean isValid(int timeout) throws UncheckedSQLException {
        return getSqlConnection().isValid(timeout);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws UncheckedSQLException {
        return getSqlConnection().prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws UncheckedSQLException {
        return getSqlConnection().prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws UncheckedSQLException {
        return getSqlConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Connection connection() {
        return getSqlConnection().connection();
    }


    private SqlConnection getSqlConnection() {
        return SqlConnectionImpl.use(DataSourceUtils.getConnection(this.dataSource));
//        return SqlConnectionImpl.use(factory.get());
    }


}
