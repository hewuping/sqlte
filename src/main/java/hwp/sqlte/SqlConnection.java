package hwp.sqlte;


import hwp.sqlte.mapper.LongMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Zero
 * Created by Zero on 2017/6/4 0004.
 */
public class SqlConnection implements AutoCloseable {

    private final Connection conn;

    private NameConverter nameConverter = NameConverter.DEFAULT;

    private final Properties SQLS = Sql.SQLS;

    private SqlConnection(Connection conn) {
        this.conn = conn;
    }

    private final static Map<Connection, SqlConnection> cache = new WeakHashMap<>();

    public static SqlConnection warp(Connection conn) {
        SqlConnection c = cache.get(conn);
        if (c == null) {
            c = new SqlConnection(conn);
            cache.put(conn, c);
        }
        return new SqlConnection(conn);
    }


    public SqlResultSet query(String sql, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            java.sql.ResultSet rs = stat.executeQuery();
            return Helper.convert(rs);
        }
    }

    public SqlResultSet query(Consumer<SqlBuilder> consumer) throws SQLException {
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        return query(sb.sql(), sb.args());
    }

    public void query(String sql, Function<ResultSet, Boolean> handler, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            java.sql.ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                if (!handler.apply(rs)) {
                    break;
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public int insert(String sql, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            return stat.executeUpdate();
        }
    }

    public SqlResultSet insertAndReturn(String sql, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql), Statement.RETURN_GENERATED_KEYS)) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            stat.executeUpdate();
            ResultSet rs = stat.getGeneratedKeys();
            if (rs != null) {
                return Helper.convert(rs);
            }
            return SqlResultSet.EMPTY;
        }
    }


    public Long insertAndReturn(String sql, String idColumn, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql), new String[]{idColumn})) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            int i = stat.executeUpdate();
            if (i > 0) {
                ResultSet rs = stat.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    return rs.getLong(idColumn);
                }
            }
            return null;
        }
    }


    public void insert(Object bean) throws Exception {
        insert(bean, bean.getClass().getSimpleName().toLowerCase());
    }

    public void insert(Object bean, String table) throws Exception {
        Field[] fields = bean.getClass().getFields();//只映射public字段，public字段必须有
        Field[] fs = new Field[fields.length];
        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                fs[count++] = field;
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("The bean must contain public fields");
        }
        String[] columns = new String[count];
        Object[] values = new Object[count];
        for (int i = 0; i < count; i++) {
            Field field = fs[i];
            columns[i] = field.getName();
            values[i] = field.get(bean);
        }
        try (PreparedStatement stat = conn.prepareStatement(Insert.make(table, columns), Statement.RETURN_GENERATED_KEYS)) {
            Helper.fillStatement(stat, values);
            int c = stat.executeUpdate();
            if (c == 0) {
                return;
            }
            ResultSet keys = stat.getGeneratedKeys();
            if (keys != null) {
                ResultSetMetaData metaData = keys.getMetaData();
                int cols = metaData.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    String name = metaData.getColumnLabel(i);
                    if (Helper.hasPublicField(bean.getClass(), name)) {
                        Field field = bean.getClass().getField(name);
                        if (!Modifier.isFinal(field.getModifiers())) {
                            field.set(bean, keys.getObject(i));
                        }
                    }
                }
            }
        }
    }

    public void insert(String table, Map<String, Object> row) throws SQLException {
        String sql = Insert.make(table, row.keySet().toArray(new String[row.size()]));
//      insert(sql, row.values().toArray());
        try (PreparedStatement stat = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Helper.fillStatement(stat, row.values().toArray());
            stat.executeUpdate();
            ResultSet keys = stat.getGeneratedKeys();
            if (keys != null) {
                ResultSetMetaData metaData = keys.getMetaData();
                int cols = metaData.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    String name = metaData.getColumnLabel(i);
                    row.put(name, keys.getObject(i));
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int update(String sql, Object... args) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(sql);
        Helper.fillStatement(statement, args);
        return statement.executeUpdate();
    }

    public int update(Consumer<SqlBuilder> consumer) throws SQLException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        PreparedStatement statement = conn.prepareStatement(builder.sql());
        Helper.fillStatement(statement, builder.args());
        return statement.executeUpdate();
    }

    public <T> void batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws SQLException {
        this.batchUpdate(sql, 1000, it, consumer);
    }

    public <T> void batchUpdate(String sql, int maxBatchSize, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            UnsafeCount count = new UnsafeCount();
            BatchExecutor executor = args -> {
                try {
                    Helper.fillStatement(statement, args);
                    statement.addBatch();
                    if (count.add(1) >= maxBatchSize) {
                        statement.executeBatch();
                    }
                } catch (SQLException e) {
                    throw new UncheckedSQLException(e);
                }
            };
            it.forEach(t -> consumer.accept(executor, t));
            if (count.get() > 0) {
                statement.executeUpdate();
            }
        }
    }

    //分批导入大量数据
    public void batchUpdate(String sql, Consumer<BatchExecutor> consumer) throws SQLException {
        this.batchUpdate(sql, 1000, consumer);
    }

    public void batchUpdate(String sql, int maxBatchSize, Consumer<BatchExecutor> consumer) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            UnsafeCount count = new UnsafeCount();
            BatchExecutor executor = args -> {
                try {
                    Helper.fillStatement(statement, args);
                    statement.addBatch();
                    if (count.add(1) >= maxBatchSize) {
                        statement.executeBatch();
                    }
                } catch (SQLException e) {
                    throw new UncheckedSQLException(e);
                }
            };
            consumer.accept(executor);
            if (count.get() > 0) {
                statement.executeUpdate();
            }
        }
    }

    public <T> void update(T bean, String table) throws Exception {
        //只映射public字段，public字段必须有
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ").append(table).append(" SET \n");
        Field[] fields = bean.getClass().getFields();
        List<Object> args = new ArrayList<>();
        String idColumn = null;
        Object idValue = null;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                builder.append(nameConverter.columnName(field)).append("=?\n");
                args.add(field.get(bean));
                Id id = field.getAnnotation(Id.class);
                if (id != null) {
                    idColumn = id.column().isEmpty() ? field.getName() : id.column();
                    idValue = field.get(bean);
                }
            }
        }
        if (fields.length == 0) {
            throw new IllegalArgumentException("The bean must contain public fields");
        }
        if (idColumn == null) {
            //warn
        } else {
            builder.append("WHERE\n").append(idColumn).append("=?");
            args.add(idValue);
        }
        update(builder.toString(), args.toArray());
    }


    /////////////////////////////


    private String toSql(String sql) {
        if (sql.startsWith("#")) {
            return SQLS.getProperty(sql);
        }
        return sql;
    }


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

    public void rollback() throws SQLException {
        conn.rollback();
    }

    public void close() throws SQLException {
        conn.close();
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


}
