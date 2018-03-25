package hwp.sqlte;


import hwp.sqlte.mapper.LongMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

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

    public Optional<Long> incInsert(String sql, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql), Statement.RETURN_GENERATED_KEYS)) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            int i = stat.executeUpdate();
            if (i > 0) {
                SqlResultSet rs = Helper.convert(stat.getGeneratedKeys());
                return rs.first(LongMapper.MAPPER);
            }
            return Optional.empty();
        }
    }

    public boolean insert(String sql, Object... args) throws SQLException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            return stat.executeUpdate() > 0;
        }
    }


    public boolean insert(Object bean, String table) throws Exception {
        Field[] fields = bean.getClass().getFields();//只映射public字段，public字段必须有
        Field[] fs = new Field[fields.length];
        int count = 0;
        Field idField = null;
        Id id = null;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                fs[count++] = field;
                //ID
                id = field.getAnnotation(Id.class);
                if (id != null) {
                    idField = field;
                }
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("The bean must contain public fields");
        }
        String[] columns = new String[count];
        Object[] values = new Object[count];
        for (int i = 0; i < count; i++) {
            Field field = fs[i];
            if (id != null && field == idField) {
                String idColumn = id.column().isEmpty() ? idField.getName() : id.column();
                columns[i] = idColumn;
            } else {
                columns[i] = field.getName();
            }
            values[i] = field.get(bean);
        }


        if (idField != null && id != null && id.auto()) {
            Optional<Long> aLong = incInsert(Insert.make(table, columns), values);
            if (aLong.isPresent()) {
                idField.set(bean, aLong.get());
                return true;
            }
            return false;
        } else {
            return insert(Insert.make(table, columns), values);
        }
    }

    public boolean insert(Object bean) throws Exception {
        return insert(bean, nameConverter.tableName(bean.getClass().getSimpleName()));
    }


    /////////////////////////////
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

    public void batchUpdate(Consumer<SqlBuilder> consumer, ArgsProvider provider) throws SQLException {
        SqlBuilder builder = new SqlBuilder();
        consumer.accept(builder);
        String sql = builder.sql();
        PreparedStatement statement = conn.prepareStatement(sql);
        int i = 0;
        while (provider.hasNext()) {
            Helper.fillStatement(statement, provider.nextArgs());
            statement.addBatch();
            if (i++ == provider.batchSize()) {
                statement.executeBatch();
            }
        }
        statement.executeUpdate();
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
