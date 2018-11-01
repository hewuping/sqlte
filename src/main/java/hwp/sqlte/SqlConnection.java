package hwp.sqlte;

import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Zero
 * Created on 2018/10/31.
 */
public interface SqlConnection extends AutoCloseable {

    void executeSqlScript(Reader reader, boolean ignoreError);

    void statement(Consumer<Statement> consumer) throws UncheckedSQLException;

    void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws UncheckedSQLException;

    SqlResultSet query(String sql, Object... args) throws UncheckedSQLException;

    SqlResultSet query(Sql sql) throws UncheckedSQLException;

    SqlResultSet query(Consumer<SqlBuilder> consumer) throws UncheckedSQLException;

    void query(Sql sql, Consumer<ResultSet> rowHandler) throws UncheckedSQLException;

    void query(Consumer<SqlBuilder> consumer, Consumer<ResultSet> rowHandler) throws UncheckedSQLException;

    void query(String sql, Consumer<ResultSet> rowHandler, Object... args) throws UncheckedSQLException;

    void query(Sql sql, RowHandler rowHandler) throws UncheckedSQLException;

    int insert(Sql sql) throws UncheckedSQLException;

    int insert(String sql, Object... args) throws UncheckedSQLException;

    int insert(String table, String columns, Object... args) throws UncheckedSQLException;

    void insert(Sql sql, Consumer<ResultSet> resultHandler) throws UncheckedSQLException;

    Long insertAndReturnKey(String sql, String idColumn, Object... args) throws UncheckedSQLException;

    void insertBean(Object bean) throws UncheckedSQLException;

    void insertBean(Object bean, String table) throws UncheckedSQLException;

    /**
     * @param bean          JavaBean
     * @param table         table name, Nullable
     * @param returnColumns
     * @throws UncheckedSQLException if a database access error occurs
     */
    void insertBean(Object bean, String table, String... returnColumns) throws UncheckedSQLException;


    int insertMap(String table, Map<String, Object> row) throws UncheckedSQLException;

    int insertMap(String table, Map<String, Object> row, String... returnColumns) throws UncheckedSQLException;

    int update(String sql, Object... args) throws UncheckedSQLException;

    int update(Consumer<SqlBuilder> consumer) throws UncheckedSQLException;

    <T> void batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException;

    void batchInsert(List<?> beans, String table) throws UncheckedSQLException;

    <T> BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException;

    BatchUpdateResult batchUpdate(PreparedStatement statement, int maxBatchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException;

    int update(Object bean, String table, Consumer<Where> where) throws UncheckedSQLException;

    int update(Map<String, Object> map, String table, Where where) throws UncheckedSQLException;

    int update(Map<String, Object> map, String table, Consumer<Where> where) throws UncheckedSQLException;

    int updateByPks(Map<String, Object> map, String table, String... pk) throws UncheckedSQLException;

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

    PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql, String[] columnNames) throws UncheckedSQLException;

    boolean isValid(int timeout) throws UncheckedSQLException;

    PreparedStatement prepareStatement(String sql) throws UncheckedSQLException;

    CallableStatement prepareCall(String sql) throws UncheckedSQLException;

    CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws UncheckedSQLException;

    Connection connection();
}
