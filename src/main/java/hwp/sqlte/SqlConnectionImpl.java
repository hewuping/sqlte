package hwp.sqlte;


import hwp.sqlte.util.ClassUtils;
import hwp.sqlte.util.ObjectUtils;
import hwp.sqlte.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created by Zero on 2017/6/4 0004.
 */
class SqlConnectionImpl extends AbstractSqlConnection {

    private static final Logger logger = LoggerFactory.getLogger(SqlConnection.class);

    private static final int defalutBatchSize = 1000;

    private final Connection conn;

    SqlConnectionImpl(Connection conn) {
        this.conn = conn;
    }

    static SqlConnection use(Connection conn) {
        return new SqlConnectionImpl(conn);
    }

    @Override
    public SqlConnection cacheable() {
        return new SqlConnectionCacheWrapper(this, Config.getConfig().getCache());
    }

    @Override
    public void executeSqlScript(Reader reader, boolean ignoreError) {
        ScriptRunner runner = new ScriptRunner(!ignoreError, getAutoCommit());
        runner.runScript(conn, reader);
    }

    @Override
    public void statement(Consumer<Statement> consumer) throws SqlteException {
        try (Statement stat = conn.createStatement()) {
            consumer.accept(stat);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void prepareStatement(String sql, Consumer<PreparedStatement> consumer) throws SqlteException {
        try (PreparedStatement stat = conn.prepareStatement(toSql(sql))) {
            consumer.accept(stat);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }


    @Override
    public SqlResultSet query(String sql) throws SqlteException {
        try (Statement stat = conn.createStatement()) {
            sql = toSql(sql);
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}", sql);
            }
            try (java.sql.ResultSet rs = stat.executeQuery(sql)) {
                return Helper.convert(rs);
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public SqlResultSet query(String sql, Object... args) throws SqlteException {
        sql = toSql(sql);
        try (PreparedStatement stat = conn.prepareStatement(sql)) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", sql, Arrays.toString(args));
            }
            try (java.sql.ResultSet rs = stat.executeQuery()) {
                return Helper.convert(rs);
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    private Row first(Class<?> clazz, Object id) {
        ClassInfo info = getClassInfo(clazz);
        String pkColumn = info.getPKColumn();
        return query("SELECT * FROM " + info.getTableName() + " WHERE " + pkColumn + "=?", id).first();
    }

    @Override
    public <T> T tryGet(Supplier<T> supplier, Object id) throws SqlteException {
        T bean = supplier.get();
        Row first = first(bean.getClass(), id);
        if (first == null) {
            return null;
        }
        return first.copyTo(bean);
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Object id) throws SqlteException {
        Row first = first(clazz, id);
        if (first == null) {
            return null;
        }
        return first.copyTo(ClassUtils.newInstance(clazz));
    }

    @Override
    public <T> T tryGet(Class<T> clazz, Consumer<Map<String, Object>> consumer) throws SqlteException {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(consumer);
        ClassInfo info = getClassInfo(clazz);
        String pkColumn = info.getPKColumn();
        Map<String, Object> map = new HashMap<>();
        consumer.accept(map);
        if (map.isEmpty()) {
            throw new IllegalArgumentException("map is empty");
        }
        List<T> list = query(sql -> {
            sql.from(info.getTableName()).where(where -> {
                map.forEach((name, value) -> {
                    where.and(name + " =?", value);
                });
            }).limit(2);
        }).list(clazz);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new SqlteException("Only one record was expected to be returned, but multiple records were returned");
    }

    public <T, E> E loadAs(Class<T> clazz, Class<E> as, Object id) {
        Row first = first(clazz, id);
        if (first == null) {
            return null;
        }
        return first.copyTo(ClassUtils.newInstance(as));
    }

    public <T> T reload(T bean) throws SqlteException {
        return this.reload(bean, null);
    }

    @Override
    public <T> T reload(T bean, String table) throws SqlteException {
        try {
            ClassInfo info = getClassInfo(bean.getClass());
            String[] pkColumns = info.getPkColumns();
            Where where = new Where();
            for (String k : pkColumns) {
                where.and(k + "=?", info.getFieldByColumn(k).get(bean));
            }
            Row first = query(sql -> sql.from(Objects.toString(table, info.getTableName())).where(where)).first();
            if (first == null) {
                return null;
            }
            return first.copyTo(bean);
        } catch (IllegalAccessException e) {
            return bean;
        }
    }


    @Override
    public void query(Sql sql, ResultSetHandler rowHandler) throws SqlteException {
        try (PreparedStatement stat = createQueryStatement(sql.sql())) {
            if (sql.args().length > 0) {
                Helper.fillStatement(stat, sql.args());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", sql, Arrays.toString(sql.args()));
            }
            try (java.sql.ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    rowHandler.accept(rs);
                }
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public long selectCount(Object example) throws SqlteException {
        String table = getTableName(example.getClass());
        return query(sql -> sql.selectCount(table).where(Where.ofExample(example).allowEmpty())).asLong();
    }


    @Override
    public void query(Sql sql, RowHandler rowHandler) throws SqlteException {
        String _sql = toSql(sql.sql());
        try (PreparedStatement stat = createQueryStatement(_sql)) {
            if (sql.args().length > 0) {
                Helper.fillStatement(stat, sql.args());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", _sql, Arrays.toString(sql.args()));
            }
            try (java.sql.ResultSet rs = stat.executeQuery()) {
                while (rs.next() && rowHandler.handle(Row.from(rs))) {

                }
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }


    private PreparedStatement createQueryStatement(String sql) throws SqlteException {
        try {
            sql = toSql(sql);
            PreparedStatement stat = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (isMySQL()) {
                //jdbc规范: rows >= 0. MySQL有个例外, 可以是Integer.MIN_VALUE
                stat.setFetchSize(Integer.MIN_VALUE);//防止查询大数据时MySQL OOM
            }
            stat.setFetchDirection(ResultSet.FETCH_FORWARD);
            return stat;
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int insert(String table, String columns, Object... args) throws SqlteException {
        return executeUpdate(Helper.makeInsertSql(table, columns), args);
    }

    @Override
    public void insert(Sql sql, ResultSetHandler resultHandler) throws SqlteException {
        String _sql = toSql(sql.sql());
        try (PreparedStatement stat = conn.prepareStatement(_sql, Statement.RETURN_GENERATED_KEYS)) {
            if (sql.args().length > 0) {
                Helper.fillStatement(stat, sql.args());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", _sql, Arrays.toString(sql.args()));
            }
            stat.executeUpdate();
            try (ResultSet rs = stat.getGeneratedKeys()) {
                if (rs != null && rs.next()) {
                    resultHandler.accept(rs);
                }
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public Long insertAndReturnKey(String sql, String idColumn, Object... args) throws SqlteException {
        sql = toSql(sql);
        try (PreparedStatement stat = conn.prepareStatement(sql, new String[]{idColumn})) {
            if (args.length > 0) {
                Helper.fillStatement(stat, args);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", sql, Arrays.toString(args));
            }
            int i = stat.executeUpdate();
            if (i > 0) {
                try (ResultSet rs = stat.getGeneratedKeys()) {
                    if (rs != null && rs.next()) {
                        try {
                            return rs.getLong(idColumn);
                        } catch (SQLException e) {
                            return rs.getLong(1);
                        }
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }


    @Override
    public void insert(Object bean, String table) throws SqlteException {
        this.insert(null, bean, table);
    }

    @Override
    public void replace(Object bean, String table) throws SqlteException {
        this.insert("REPLACE INTO", bean, table);
    }

    @Override
    public void insertIgnore(Object bean, String table) throws SqlteException {
        this.insert("INSERT IGNORE INTO", bean, table);
    }

    private void insert(String insert, Object bean, String table) throws SqlteException {
        ClassInfo info = getClassInfo(bean.getClass());
        if (table == null) {
            table = info.getTableName();
        }
        Map<String, Field> columnFieldMap = info.getColumnFieldMap();
        if (columnFieldMap.isEmpty()) {
            throw new IllegalArgumentException("The bean must contain public fields");
        }
        List<String> columns = new ArrayList<>(columnFieldMap.size());
        List<Object> values = new ArrayList<>(columnFieldMap.size());
        try {
            for (Map.Entry<String, Field> entry : columnFieldMap.entrySet()) {
                Field field = entry.getValue();
                Object v = Helper.getSerializedValue(bean, field);
                if (v != null) {
                    columns.add(entry.getKey());
                    values.add(v);//enum->int
                }
            }
        } catch (Exception e) {
            throw new SqlteException(e);
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("The bean must contain public fields and value is not null");
        }

        String[] returnColumns = info.getAutoGenerateColumns();

        String sql = Helper.makeInsertSql(insert, table, columns.toArray(new String[0]));
        //Statement.RETURN_GENERATED_KEYS
        try (PreparedStatement stat = returnColumns == null || returnColumns.length == 0 ? conn.prepareStatement(sql)
                : conn.prepareStatement(sql, returnColumns)) {// new String[]{"id"}
            Helper.fillStatement(stat, values.toArray(new Object[0]));
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", sql, values);
            }
            int c = stat.executeUpdate();
            if (c == 0) {
                return;
            }
            if (returnColumns == null || returnColumns.length == 0) {
                return;
            }
            try (ResultSet keys = stat.getGeneratedKeys()) {
                if (keys != null && keys.next()) {
                    this.getGeneratedKeysAndSet(info, bean, keys);
                }
            } catch (IllegalAccessException e) {
                throw new SqlteException(e);
            }
        } catch (SQLException e) {
            //java.sql.SQLSyntaxErrorException: Unknown column 'xxx' in 'field list'
            throw new SqlteException(e);
        }
    }


    /**
     * 该方法暂时未使用
     */
    private <T> BatchUpdateResult batchInsert0(Class<T> clazz, List<T> beans, String table, SqlHandler sqlHandler) throws SqlteException {
        // beans 应该是已分好批次的列表
        if (beans.isEmpty()) {
            return BatchUpdateResult.EMPTY;
        }
        for (T o : beans) {
            if (o.getClass() != clazz.getClass()) {
                throw new IllegalArgumentException("The object type in the collection must be consistent");
            }
        }
        ClassInfo info = getClassInfo(clazz);
        String[] columns = info.getInsertColumns();
        String sql = sqlHandler == null ? Helper.makeInsertSql(table, columns) : sqlHandler.handle(Helper.makeInsertSql(table, columns));
        try (PreparedStatement stat = conn.prepareStatement(sql, info.getAutoGenerateColumns())) {
            for (T bean : beans) {
                Object[] args = new Object[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    Field field = info.getFieldByColumn(columns[i]);
                    args[i] = Helper.getSerializedValue(bean, field);
                }
                Helper.fillStatement(stat, args);
                stat.addBatch();
            }
            int[] rs = stat.executeBatch();
            if (ObjectUtils.isNotEmpty(info.getAutoGenerateColumns())) {
                getGeneratedKeysAndSet(beans.iterator(), stat.getGeneratedKeys());
            }
            BatchUpdateResult result = new BatchUpdateResult();
            result.addBatchResult(rs);
            return result;
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BatchUpdateResult batchInsert(List<T> beans, String table, SqlHandler sqlHandler) throws SqlteException {
        if (beans.isEmpty()) {
            return BatchUpdateResult.EMPTY;
        }
        T first = beans.get(0);
        for (T o : beans) {
            if (o.getClass() != first.getClass()) {
                throw new IllegalArgumentException("The object type in the collection must be consistent");
            }
        }
        Class<T> aClass = (Class<T>) first.getClass();
        ClassInfo info = getClassInfo(aClass);
        Iterator<T> it = beans.iterator();
        return batchInsert(consumer -> beans.forEach(consumer::accept), aClass, table, sqlHandler, genKeysRs -> {
            try {
                String[] agc = info.getAutoGenerateColumns();
                if (agc == null || agc.length == 0) {
                    return;
                }
                this.getGeneratedKeysAndSet(it, genKeysRs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public <T> BatchUpdateResult batchInsert(DataLoader<T> loader, Class<T> clazz, String table, SqlHandler sqlHandler, GeneratedKeysConsumer genKeysConsumer) throws SqlteException {
        ClassInfo info = getClassInfo(clazz);
        if (table == null) {
            table = info.getTableName();
        }
        String[] columns = info.getInsertColumns();
        String sql = sqlHandler == null ? Helper.makeInsertSql(table, columns) : sqlHandler.handle(Helper.makeInsertSql(table, columns));
        try (PreparedStatement stat = conn.prepareStatement(sql, info.getAutoGenerateColumns())) {
            return batchUpdate(stat, 500, executor -> {
                loader.load(bean -> {
                    Object[] args = new Object[columns.length];
                    for (int i = 0; i < columns.length; i++) {
                        Field field = info.getFieldByColumn(columns[i]);
                        args[i] = Helper.getSerializedValue(bean, field);
                    }
                    executor.exec(args);
                });
            }, genKeysConsumer);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public int insertMap(String table, Map<String, Object> row) throws SqlteException {
        return insertMap(table, row, (String[]) null);
    }

    @Override
    public int insertMap(String table, Consumer<Row> row) throws SqlteException {
        Row _map = new Row();
        row.accept(_map);
        return this.insertMap(table, _map);
    }

    @Override
    public int insertMap(String table, Map<String, Object> row, String... returnColumns) throws SqlteException {
        return this.insertMap("INSERT INTO", table, row, returnColumns);
    }

    @Override
    public int replaceMap(String table, Map<String, Object> row, String... returnColumns) {
        return this.insertMap("REPLACE INTO", table, row, returnColumns);
    }

    private int insertMap(String insert, String table, Map<String, Object> row, String... returnColumns) throws SqlteException {
        String sql = Helper.makeInsertSql(insert, table, row.keySet().toArray(new String[0]));
//      insert(sql, row.values().toArray());
        try (PreparedStatement stat = (returnColumns == null ? conn.prepareStatement(sql)
                : conn.prepareStatement(sql, returnColumns))) {//Statement.RETURN_GENERATED_KEYS
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", sql, row.values());
            }
            Helper.fillStatement(stat, row.values().toArray());
            int uc = stat.executeUpdate();
            if (uc == 0) {
                return 0;
            }
            if (returnColumns != null && returnColumns.length > 0) {
                try (ResultSet keys = stat.getGeneratedKeys()) {
                    boolean isMysql = isMySQL();
                    if (keys != null && keys.next()) {
                        ResultSetMetaData metaData = keys.getMetaData();
                        int cols = metaData.getColumnCount();
                        for (int i = 1; i <= cols; i++) {
                            String name = metaData.getColumnLabel(i);
                            //mysql会返回GENERATED_KEY, 没有实现JDBC规范
                            if ("GENERATED_KEY".equals(name) && isMysql) {
                                row.put(returnColumns[0].toLowerCase(), keys.getObject(i));
                                break;
                            }
                            //pgsql如果设置了列名, 则返回指定列, RETURN_GENERATED_KEYS会返回所有列
                            row.put(name.toLowerCase(), keys.getObject(i));
                        }
                    }
                }
                return uc;
            }
            return uc;
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int executeUpdate(String sql, Object... args) throws SqlteException {
        try {
            sql = toSql(sql);
            if (logger.isDebugEnabled()) {
                logger.debug("sql: {}\t args: {}", sql, Arrays.toString(args));
            }
            if (args.length == 0) {
                try (Statement statement = conn.createStatement()) {
                    return statement.executeUpdate(sql);
                }
            }
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                Helper.fillStatement(statement, args);
                return statement.executeUpdate();
            }
        } catch (Exception e) {
            throw new SqlteException(e);
        }
    }


    @Override
    public boolean update(Object bean, UpdateOptions options) throws SqlteException {
        try {
            ClassInfo info = getClassInfo(bean.getClass());

            String columns = options.columns();
            String table = Objects.toString(options.table(), info.getTableName());
            String[] _columns;
            if (StringUtils.isBlank(columns)) {
                _columns = info.getUpdateColumns();
            } else {
                _columns = StringUtils.splitToArray(columns);
            }

            if (_columns.length == 0) {
                throw new IllegalArgumentException("No fields to modify: " + columns);
            }

            Object[] args = new Object[_columns.length];
            int nullCount = 0;
            for (int i = 0; i < _columns.length; i++) {
                String column = _columns[i];
                Field field = info.getFieldByColumn(column);
                if (field == null) {
                    throw new IllegalArgumentException("No field mapping: " + column);
                }
                args[i] = Helper.getSerializedValue(bean, field);
                if (args[i] == null) {
                    nullCount++;
                }
            }
            if (options.isIgnoreNullValues() && nullCount > 0) {
                int updateColumnCount = _columns.length - nullCount;
                if (updateColumnCount < 1) {
//                    throw new UncheckedException("No fields to update");
                    return false;
                }
                String[] newColumns = new String[updateColumnCount];
                Object[] newArgs = new Object[updateColumnCount];
                for (int i = 0, ci = 0; i < args.length; i++) {
                    Object v = args[i];
                    if (v != null) {
                        newColumns[ci] = _columns[i];
                        newArgs[ci] = v;
                        ci++;
                    }
                }
                args = newArgs;
                _columns = newColumns;
            }
            String sql = Helper.makeUpdateSql(table, _columns, null);
            SqlBuilder builder = new SqlBuilder();
            builder.append(sql, args);

            Where where = new Where();
            String[] pkColumns = info.getPkColumns();
            if (pkColumns.length == 0) {
                throw new IllegalArgumentException("No key field mapping for " + bean.getClass().getName());
            }
            for (String k : pkColumns) {
                Field field = info.getFieldByColumn(k);
                Object idValue = field.get(bean);
                if (idValue == null) {
                    throw new IllegalArgumentException("Key field value is null: " + field.getName());
                }
                where.and(k + "=?", idValue);
            }
            where.check();
            builder.where(where);
            return executeUpdate(builder.sql(), builder.args()) == 1;
        } catch (IllegalAccessException e) {
            //Never happen
            return false;
        }
    }


    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, Iterable<T> it, BiConsumer<BatchExecutor, T> consumer)
            throws SqlteException {
//        return batchUpdate(sql, defalutBatchSize, executor -> it.forEach(item -> consumer.accept(executor, item)), null);
        return this.batchUpdate(sql, defalutBatchSize, it, consumer);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(String sql, int batchSize, Iterable<
            T> it, BiConsumer<BatchExecutor, T> consumer) throws SqlteException {
        return batchUpdate(sql, batchSize, executor -> it.forEach(item -> consumer.accept(executor, item)));
    }

    //分批导入大量数据
    @Override
    public BatchUpdateResult batchUpdate(String sql, Consumer<BatchExecutor> consumer)
            throws SqlteException {
        return this.batchUpdate(sql, defalutBatchSize, consumer);
    }


    @Override
    public BatchUpdateResult batchInsert(String table, String columns, Consumer<BatchExecutor> consumer)
            throws SqlteException {
        String sql = Helper.makeInsertSql(table, columns);
        return this.batchUpdate(sql, consumer);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer) throws
            SqlteException {
        return batchUpdate(sql, batchSize, consumer, null);
    }

    @Override
    public BatchUpdateResult batchUpdate(String sql, int batchSize, Consumer<BatchExecutor> consumer, GeneratedKeysConsumer genKeysConsumer) throws
            SqlteException {
        sql = toSql(sql);
        if (logger.isDebugEnabled()) {
            logger.debug("sql: {}", sql);
        }

//        try (PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
        try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            return batchUpdate(statement, batchSize, consumer, genKeysConsumer);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(PreparedStatement statement, int batchSize, Consumer<BatchExecutor> consumer, GeneratedKeysConsumer genKeysConsumer) throws SqlteException {
        try {
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit) {
                conn.setAutoCommit(false);
            }
            Savepoint savepoint = conn.setSavepoint("batchUpdate");
            BatchUpdateResult result = new BatchUpdateResult();
            Counter count = new Counter();
            BatchExecutor executor = new BatchExecutor() {
                @Override
                public void exec(Object... args) {
                    try {
                        Helper.fillStatement(statement, args);
                        statement.addBatch();
                        if (count.add(1) >= batchSize) {
                            int[] rs0 = statement.executeBatch();
                            result.addBatchResult(rs0);
                            if (genKeysConsumer != null) {
                                genKeysConsumer.accept(statement.getGeneratedKeys());
                            }
                            count.reset();
                        }
                        statement.clearParameters();
                    } catch (SQLException e) {
                        throw new SqlteException(e);
                    }
                }
            };
            consumer.accept(executor);
            if (count.get() > 0) {
                int[] rs0 = statement.executeBatch();
                result.addBatchResult(rs0);
                if (genKeysConsumer != null) {
                    genKeysConsumer.accept(statement.getGeneratedKeys());
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
            throw new SqlteException(e);
        }

    }

    public <T> BatchUpdateResult batchUpdate(List<T> beans, String table) throws SqlteException {
        return this.batchUpdate(beans, table, null);
    }

    @Override
    public <T> BatchUpdateResult batchUpdate(List<T> beans, String table, String _columns) throws SqlteException {
        if (beans.isEmpty()) {
            return BatchUpdateResult.EMPTY;
        }
        Object first = beans.get(0);
        ClassInfo info = getClassInfo(first.getClass());
        if (table == null) {
            table = info.getTableName();
        }
        String[] columns;// 可更新的列
        if (StringUtils.isNotBlank(_columns)) {
            columns = StringUtils.splitToArray(_columns);
        } else {
            columns = info.getUpdateColumns();
        }
        if (columns.length == 0) {
            throw new IllegalArgumentException("No fields to modify: " + columns);
        }
        String[] pkColumns = info.getPkColumns();// ID
        String sql = Helper.makeUpdateSql(table, columns, pkColumns);
        return batchUpdate(sql, beans, (executor, item) -> {
            List<Object> args = new ArrayList<>(columns.length + pkColumns.length);
            for (String column : columns) {
                Field field = info.getFieldByColumn(column);
                Object value = Helper.getSerializedValue(item, field);
                args.add(value);
            }
            for (String column : pkColumns) {
                Field field = info.getFieldByColumn(column);
                Object value = Helper.getSerializedValue(item, field);
                args.add(value);
            }
            executor.exec(args.toArray());
        });
    }


    @Override
    public boolean delete(Object bean, String table) throws SqlteException {
        BatchUpdateResult result = this.batchDelete(Arrays.asList(bean), table);
        return result.affectedRows == 1;
    }

    public <T> BatchUpdateResult batchDelete(List<T> beans, String table) throws SqlteException {
        try {
            Objects.requireNonNull(beans);
            if (beans.isEmpty()) {
                return BatchUpdateResult.EMPTY;
            }
            Object first = beans.get(0);
            ClassInfo info = getClassInfo(first.getClass());
            if (table == null) {
                table = info.getTableName();
            }
            String[] pkColumns = info.getPkColumns();
            if (pkColumns.length == 0) {
                throw new SqlteException("The class unspecified ID field: " + info.className());
            }
            StringBuilder builder = new StringBuilder();
            builder.append("DELETE FROM ").append(table);
            builder.append(" WHERE ");
            for (String pkColumn : pkColumns) {
                if (builder.indexOf("=") > 0) {
                    builder.append(" AND ");
                }
                builder.append(pkColumn + "=?");
            }
            String sql = builder.toString();
            return this.batchUpdate(sql, beans, (executor, bean) -> {
                Object[] values = info.getValueByColumns(bean, pkColumns);
                for (Object value : values) {
                    Objects.requireNonNull(value, "value must not be null");
                }
                executor.exec(values);
            });
        } catch (Exception e) {
            throw SqlteException.warp(e);
        }
    }


    @Override
    public int update(String table, Map<String, Object> data, Where where) throws SqlteException {
        Objects.requireNonNull(where, "where must not be null");
        where.check();
        SqlBuilder builder = new SqlBuilder();
        builder.update(table, data).where(where);
        try (PreparedStatement statement = conn.prepareStatement(builder.sql())) {
            if (logger.isDebugEnabled()) {
                logger.debug("update: {}\t args: {}", builder.sql(), Arrays.toString(builder.args()));
            }
            Helper.fillStatement(statement, builder.args());
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

//    /**
//     * @param table table name
//     * @param map   data
//     * @param ids   default name is "id"
//     * @throws SqlteException if a database access error occurs
//     */
//    @Override
//    public int updateByPks(String table, Map<String, Object> map, String... ids) throws SqlteException {
//        return update(table, new HashMap<>(map), where -> {
//            if (ids.length == 0) {
//                Object v = map.get("id");
//                if (v == null) {
//                    throw new IllegalArgumentException("Primary key columns not specified, and the default id column does not have a value");
//                }
//                where.and("id=?", v);
//            } else {
//                for (String key : ids) {
//                    Object v = map.get(key);
//                    if (v == null) {
//                        throw new IllegalArgumentException("Values in primary key columns cannot be empty: " + key);
//                    }
//                    where.and(key + "=?", v);
//                }
//            }
//        });
//    }


    ///////////////////////////////////////////////////////////////////////////
    // 委托方法
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void setAutoCommit(boolean autoCommit) throws SqlteException {
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public boolean getAutoCommit() throws SqlteException {
        try {
            return conn.getAutoCommit();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void commit() throws SqlteException {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void rollback() throws SqlteException {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void close() throws SqlteException {
        try {
            conn.setAutoCommit(true);
            conn.close();
            if (logger.isDebugEnabled()) {
                logger.debug("SqlConnection closed");
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public boolean isClosed() throws SqlteException {
        try {
            return conn.isClosed();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }


    @Override
    public void setReadOnly(boolean readOnly) throws SqlteException {
        try {
            conn.setReadOnly(readOnly);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public boolean isReadOnly() throws SqlteException {
        try {
            return conn.isReadOnly();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws SqlteException {
        try {
            conn.setTransactionIsolation(level);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public int getTransactionIsolation() throws SqlteException {
        try {
            return conn.getTransactionIsolation();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public SqlConnection beginTransaction() throws SqlteException {
        try {
            conn.setAutoCommit(false);
            return this;
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public SqlConnection beginTransaction(int level) throws SqlteException {
        try {
            conn.setTransactionIsolation(level);
            conn.setAutoCommit(false);
            return this;
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public Savepoint setSavepoint() throws SqlteException {
        try {
            return conn.setSavepoint();
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws SqlteException {
        try {
            return conn.setSavepoint(name);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SqlteException {
        try {
            conn.rollback(savepoint);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SqlteException {
        try {
            conn.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SqlteException {
        try {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }


    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SqlteException {
        try {
            return conn.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SqlteException {
        try {
            return conn.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public boolean isValid(int timeout) throws SqlteException {
        try {
            return conn.isValid(timeout);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SqlteException {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SqlteException {
        try {
            return conn.prepareCall(sql);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            throws SqlteException {
        try {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    @Override
    public Connection connection() {
        return this.conn;
    }

    private String toSql(String sql) {
        if (sql.charAt(0) == '#') {
            return Config.getConfig().getSqlProvider().getSql(sql.substring(1));
        }
        return sql;
    }

    // ----------------------
    private boolean isMySQL() throws SQLException {
        String driverName = conn.getMetaData().getDriverName().toLowerCase();
        return driverName.contains("mysql");
    }

    private Boolean _isOnlyGenerateID;//通过 isOnlyGenerateID() 访问该属性

    private boolean isOnlyGenerateID() throws SQLException {
        if (_isOnlyGenerateID == null) {
            String driverName = conn.getMetaData().getDriverName().toLowerCase();
            _isOnlyGenerateID = driverName.contains("sqlite") || driverName.contains("mysql");
        }
        return _isOnlyGenerateID;
    }

    private <T> void getGeneratedKeysAndSet(Iterator<T> it, ResultSet generatedKeys) throws SQLException {
        try {
            // SQLite:last_insert_rowid()
            // MySQL:GENERATED_KEY
            ClassInfo info = null;
            while (generatedKeys.next() && it.hasNext()) {
                T bean = it.next();
                if (info == null) {
                    info = getClassInfo(bean.getClass());
                }
                getGeneratedKeysAndSet(info, bean, generatedKeys);
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new SqlteException(e);
        }
    }

    private void getGeneratedKeysAndSet(ClassInfo info, Object bean, ResultSet keys) throws SQLException, IllegalAccessException {
        String[] returnColumns = info.getAutoGenerateColumns();
        //Modifier.isFinal(field.getModifiers())
        //MySQL: BigInteger
        ResultSetMetaData metaData = keys.getMetaData();
        int cols = metaData.getColumnCount();
//      ConversionService conversion = Config.getConfig().getConversionService();
        for (int i = 1; i <= cols; i++) {
            String name = metaData.getColumnLabel(i);
            if (isOnlyGenerateID()) {
                String idColumn = returnColumns[0];
                Field f = info.getFieldByColumn(idColumn);
                if (f != null) {
                    Object id = keys.getObject(1, f.getType());//bug: MySQL driver 5.1.6 is not support
                    f.set(bean, id);
                }
                break;
            }
            for (String column : info.getColumns()) {
                if (column.equalsIgnoreCase(name)) {
                    Field f = info.getFieldByColumn(column);
                    if (f != null) {
                        Object id = keys.getObject(i, f.getType());
                        f.set(bean, id);
                    }
                    break;
                }
            }
        }
    }


}
