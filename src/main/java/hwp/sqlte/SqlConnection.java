package hwp.sqlte;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Zero
 *         Created by Zero on 2017/6/4 0004.
 */
public class SqlConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger("sqlte");

    private final Connection conn;

//    private NameConverter nameConverter = NameConverter.DEFAULT;

    private SqlConnection(Connection conn) {
        this.conn = conn;
    }

    private final static Map<Connection, SqlConnection> cache = new WeakHashMap<>();

    public static SqlConnection use(Connection conn) {
        SqlConnection c = cache.get(conn);
        if (c == null) {
            c = new SqlConnection(conn);
            cache.put(conn, c);
        }
        return new SqlConnection(conn);
    }


    public SqlResultSet query(String sql, Object... args) throws UncheckedSQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            try (java.sql.ResultSet rs = stat.executeQuery()) {
                return Helper.convert(rs);
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public SqlResultSet query(Sql sql) throws UncheckedSQLException {
        return query(sql.sql(), sql.args());
    }

    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        return query(sb.sql(), sb.args());
    }

    public void query(Sql sql, Consumer<ResultSet> rowHandler) throws UncheckedSQLException {
        query(sql.sql(), rowHandler, sql.args());
    }

    public void query(Consumer<SqlBuilder> consumer, Consumer<ResultSet> rowHandler) throws UncheckedSQLException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        query(sb.sql(), rowHandler, sb.args());
    }

    public void query(String sql, Consumer<ResultSet> rowHandler, Object... args) throws UncheckedSQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            try (java.sql.ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    rowHandler.accept(rs);
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    /**
     * @param sql
     * @param rowHandler Stop if it returns false
     * @throws UncheckedSQLException
     */
    public void query(Sql sql, RowHandler rowHandler) throws UncheckedSQLException {
        try (PreparedStatement stat = conn.prepareStatement(sql.sql())) {
            if (sql.args().length > 0) {
                Helper.fillStatement(stat, sql.args());
            }
            try (java.sql.ResultSet rs = stat.executeQuery()) {
                while (rs.next() && rowHandler.handle(Row.from(rs))) {

                }
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int insert(Sql sql) throws UncheckedSQLException {
        return this.insert(sql.sql(), sql.args());
    }

    public int insert(String sql, Object... args) throws UncheckedSQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            return stat.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void insert(Sql sql, Consumer<ResultSet> resultHandler) throws UncheckedSQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql.sql()), Statement.RETURN_GENERATED_KEYS)) {
            if (sql.args().length > 0) {
                Helper.fillStatement(stat, sql.args());
            }
            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                if (rs != null && rs.next()) {
                    resultHandler.accept(rs);
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }

    }

    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws UncheckedSQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql), new String[]{idColumn})) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            int i = stat.executeUpdate();
            if (i > 0) {
                try (ResultSet rs = stat.getGeneratedKeys()) {
                    if (rs != null && rs.next()) {
                        return rs.getLong(idColumn);
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void insertBean(Object bean) throws Exception {
        this.insertBean(bean, bean.getClass().getSimpleName().toLowerCase());
    }

    public void insertBean(Object bean, String table) throws Exception {
        this.insertBean(bean, table, (String[]) null);
    }


    public void insertBean(Object bean, String table, String... returnColumns) throws UncheckedSQLException {
        Field[] fields = bean.getClass().getFields();//只映射public字段，public字段必须有
        Field[] fs = new Field[fields.length];
        int count = 0;
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                fs[count++] = field;
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("The bean must contain public fields");
        }
        List<String> columns = new ArrayList<>(count);
        List<Object> values = new ArrayList<>(count);
        try {
            for (int i = 0; i < count; i++) {
                Field field = fs[i];
                Object v = field.get(bean);
                if (v != null) {
                    columns.add(field.getName());
                    values.add(field.get(bean));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            //Never happen
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("The bean must contain public fields and value is not null");
        }
        String sql = Helper.makeInsertSql(table, columns.toArray(new String[columns.size()]));
        //Statement.RETURN_GENERATED_KEYS
        try (PreparedStatement stat = returnColumns == null || returnColumns.length == 0 ? conn.prepareStatement(sql)
                : conn.prepareStatement(sql, returnColumns)) {// new String[]{"id"}
            Helper.fillStatement(stat, values.toArray(new Object[values.size()]));
            int c = stat.executeUpdate();
            if (c == 0) {
                return;
            }
            if (returnColumns == null || returnColumns.length == 0) {
                return;
            }
            try (ResultSet keys = stat.getGeneratedKeys()) {
                if (keys != null && keys.next()) {
                    //Field field = bean.getClass().getField(idName);/
                    //Modifier.isFinal(field.getModifiers())
                    //MySQL: BigInteger
                    ResultSetMetaData metaData = keys.getMetaData();
                    int cols = metaData.getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        String name = metaData.getColumnLabel(i);
//                    System.out.println(name + " --> " + keys.getObject(name));
                        //SQLite:last_insert_rowid()
                        //MySQL:GENERATED_KEY
                        String driverName = conn.getMetaData().getDriverName().toLowerCase();
                        if (driverName.contains("sqlite") || driverName.contains("mysql")) {
                            String idColumn = returnColumns[0];
                            Field f = bean.getClass().getField(idColumn);
                            if (f.getType() == Long.TYPE || f.getType() == Long.class) {
                                f.set(bean, keys.getLong(i));
                            } else {
                                f.set(bean, keys.getInt(i));
                            }
                            //if nullColumns.size()>1: select xxx from table where id=?
                            break;
                        }
//                        if (Helper.hasPublicField(bean.getClass(), name)) {
                        Field f = bean.getClass().getField(name);
//                          f.set(bean, keys.getObject(i, f.getType()));
                        f.set(bean, keys.getObject(i));
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new UncheckedException(e);
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }


    public void insertBeans(List<? extends Object> beans, String table) throws Exception {
        if (beans.isEmpty()) {
            return;
        }
        Object first = beans.get(0);
        for (Object o : beans) {
            if (o.getClass() != first.getClass()) {
                throw new IllegalArgumentException("The object type in the collection must be consistent");
            }
        }

        Field[] fields = first.getClass().getFields();//只映射public字段，public字段必须有
        Field[] fs = new Field[fields.length];
        int count = 0;
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                fs[count++] = field;
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("The bean must contain public fields");
        }
        String[] columns = new String[count];
        for (int i = 0; i < count; i++) {
            Field field = fs[i];
            columns[i] = field.getName();
        }

        String sql = Helper.makeInsertSql(table, columns);
        batchUpdate(sql, 100, executor -> {
            beans.forEach(obj -> {
                try {
                    Object[] args = new Object[columns.length];
                    for (int i = 0; i < columns.length; i++) {
                        args[i] = fs[i].get(obj);
                    }
                    executor.exec(args);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    //ignore
                    if (logger.isDebugEnabled()) {
                        logger.debug("batchUpdate error", e);
                    }
                }
            });
        });
    }


    public int insertMap(String table, Map<String, Object> row) throws UncheckedSQLException {
        return insertMap(table, row, (String[]) null);
    }

    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws UncheckedSQLException {
        try {
            String sql = Helper.makeInsertSql(table, row.keySet().toArray(new String[row.size()]));
//      insert(sql, row.values().toArray());
            try (PreparedStatement stat = (returnColumns == null
                    ? conn.prepareStatement(sql)
                    : conn.prepareStatement(sql, returnColumns))) {//Statement.RETURN_GENERATED_KEYS
                Helper.fillStatement(stat, row.values().toArray());
                int uc = stat.executeUpdate();
                if (uc == 0) {
                    return 0;
                }
                try (ResultSet keys = stat.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        ResultSetMetaData metaData = keys.getMetaData();
                        int cols = metaData.getColumnCount();
                        for (int i = 1; i <= cols; i++) {
                            String name = metaData.getColumnLabel(i);
                            //mysql会返回GENERATED_KEY, 没有完全实现JDBC规范
                            //pgsql如果设置了列名, 则返回指定列, RETURN_GENERATED_KEYS会返回所有列
                            row.put(name, keys.getObject(i));
                        }
                    }
                }
                return uc;
            }
        } catch (SQLException e) {
            throw new UncheckedException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int update(String sql, Object... args) throws UncheckedSQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            Helper.fillStatement(statement, args);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedException(e);
        }
    }

    public int update(Consumer<SqlBuilder> consumer) throws UncheckedSQLException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        try (PreparedStatement statement = conn.prepareStatement(builder.sql())) {
            Helper.fillStatement(statement, builder.args());
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedException(e);
        }
    }

    public <T> void batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        this.batchUpdate(sql, 1000, it, consumer);
    }

    public <T> BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws UncheckedSQLException {
        return batchUpdate(sql, maxBatchSize, executor -> {
            it.forEach(t -> consumer.accept(executor, t));
        });
    }

    //分批导入大量数据
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        return this.batchUpdate(sql, 1000, consumer);
    }

    public BatchUpdateResult batchUpdate(String sql, int maxBatchSize, Consumer<BatchExecutor> consumer) throws UncheckedSQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            return batchUpdate(statement, maxBatchSize, consumer, null);
        } catch (SQLException e) {
            throw new UncheckedException(e);
        }
    }

    public BatchUpdateResult batchUpdate(PreparedStatement statement, int maxBatchSize, Consumer<BatchExecutor> consumer, BiConsumer<PreparedStatement, int[]> psConsumer) throws UncheckedSQLException {
        try {
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit) {
                conn.setAutoCommit(false);
            }
            Savepoint savepoint = conn.setSavepoint("batchUpdate");
            BatchUpdateResult result = new BatchUpdateResult();
            UnsafeCount count = new UnsafeCount();
            BatchExecutor executor = args -> {
                try {
                    Helper.fillStatement(statement, args);
                    statement.addBatch();
                    if (count.add(1) >= maxBatchSize) {
                        int[] rs0 = statement.executeBatch();
                        updateBatchUpdateResult(result, rs0);
                        if (psConsumer != null) {
                            psConsumer.accept(statement, rs0);
                        }
                        count.reset();
                    }
                    statement.clearParameters();
                } catch (SQLException e) {
                    throw new UncheckedSQLException(e);
                }
            };
            consumer.accept(executor);
            if (count.get() > 0) {
                int[] rs0 = statement.executeBatch();
                updateBatchUpdateResult(result, rs0);
                if (psConsumer != null) {
                    psConsumer.accept(statement, rs0);
                }
            }
            if (autoCommit) {
                try {
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback(savepoint);
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new UncheckedException(e);
        }

    }

    private void updateBatchUpdateResult(BatchUpdateResult result, int[] rs) {
        for (int r : rs) {
            if (r > 0) {
                result.affectedRows += r;
            } else if (r == Statement.SUCCESS_NO_INFO) {
                result.successNoInfoCount++;
            } else if (r == Statement.EXECUTE_FAILED) {
                result.failedCount++;
            }
        }
    }

    public <T> void updateBean(String table, T bean, String key, boolean ignoreNulls) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ").append(table).append(" SET ");
        Field[] fields = bean.getClass().getFields();
        List<Object> args = new ArrayList<>();
        Object idValue = null;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                Object value = field.get(bean);
                if (ignoreNulls && value == null) {
                    continue;
                }
                if (field.getName().equalsIgnoreCase(key)) {
                    idValue = field.get(bean);
                    continue;
                }
                if (args.size() > 0) {
                    builder.append(',');
                }
                builder.append(field.getName()).append("=? ");
                args.add(field.get(bean));
            }
        }
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Cannot found updateable fields");
        }
        if (idValue == null) {
            throw new IllegalArgumentException("The '" + key + "' field value cannot be null");
        }
        builder.append("WHERE ").append(key).append("=?");
        args.add(idValue);
        update(builder.toString(), args.toArray());
    }

    public int update(Map<String, Object> map, String table, Where where) throws UncheckedSQLException {
        SqlBuilder builder = new SqlBuilder();
        builder.add("UPDATE ").add(table).add(" SET ");
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            builder.add(entry.getKey()).add("=?", entry.getValue());
            if (it.hasNext()) {
                builder.add(", ");
            }
        }
        builder.where(where);
        try (PreparedStatement statement = conn.prepareStatement(builder.sql())) {
            Helper.fillStatement(statement, builder.args());
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedException(e);
        }
    }

    public void update(Map<String, Object> map, String table, Consumer<Where> where) throws UncheckedSQLException {
        Where w = new Where();
        where.accept(w);
        update(map, table, w);
    }

    /**
     * @param map   data
     * @param table table name
     * @param ids   default name is "id"
     * @throws UncheckedSQLException
     */
    public void updateById(Map<String, Object> map, String table, String... ids) throws UncheckedSQLException {
        update(new HashMap<>(map), table, where -> {
            if (ids.length == 0) {
                Object v = map.remove("id");
                if (v == null) {
                    throw new IllegalArgumentException("id is required");
                }
                where.and("id=?", v);
            } else {
                for (String key : ids) {
                    Object v = map.remove(key);
                    if (v == null) {
                        throw new IllegalArgumentException("Can't found not null value by key: " + key);
                    }
                    where.and(key + "=?", v);
                }
            }
        });
    }

    /////////////////////////////


    private String toSql(String sql) {
        if (sql.startsWith("#")) {
            return Config.SQLS.getProperty(sql);
        }
        return sql;
    }

    /////////

    ///////////////////////////////////////////////////////////////////////////
    // 委托方法
    ///////////////////////////////////////////////////////////////////////////

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws UncheckedSQLException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void close() throws UncheckedSQLException {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public boolean isClosed() throws SQLException {
        return conn.isClosed();
    }


    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        conn.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }

    public SqlConnection beginTransaction() throws SQLException {
        conn.setAutoCommit(false);
        return this;
    }

    public SqlConnection beginTransaction(int level) throws SQLException {
        conn.setTransactionIsolation(level);
        conn.setAutoCommit(false);
        return this;
    }

    public Savepoint setSavepoint() throws SQLException {
        return conn.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }


    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return conn.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return conn.prepareStatement(sql, columnNames);
    }

    public boolean isValid(int timeout) throws SQLException {
        return conn.isValid(timeout);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Connection connection() {
        return this.conn;
    }

}
