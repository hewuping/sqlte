package hwp.sqlte;


import hwp.sqlte.cache.FifoCache;
import hwp.sqlte.util.ClassUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.Instant;
import java.util.Date;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
class Helper {
    private static final FifoCache<Converter> cache = new FifoCache<>(1024);

    static Converter getConverter(Class<? extends Converter> clazz) {
        try {
            Converter converter = cache.get(clazz);
            if (converter == null) {
                converter = ClassUtils.newInstance(clazz);
                cache.put(clazz, converter);
            }
            return converter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static SqlResultSet convert(java.sql.ResultSet rs) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        List<ColumnMetaData> cmds = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int cols = metaData.getColumnCount();
        for (int column = 1; column <= cols; column++) {
            // MetaData
            ColumnMetaData cmd = new ColumnMetaData(metaData.getSchemaName(column), metaData.getTableName(column), column);
            cmd.setLabel(metaData.getColumnLabel(column));
            cmd.setName(metaData.getColumnName(column));
            cmd.setType(metaData.getColumnType(column));
            cmds.add(cmd);
            // columnNames
            columnNames.add(cmd.getLabel().toLowerCase().intern());
        }
        List<Row> results = new ArrayList<>();
        while (rs.next()) {
            Row row = new Row();
            for (int i = 1; i <= cols; i++) {
                row.put(columnNames.get(i - 1), rs.getObject(i));
            }
            results.add(row);
        }
        return new SqlResultSet(columnNames, results, cmds);
    }

    static void fillStatement(PreparedStatement statement, Object[] args) throws SqlteException {
        try {
            for (int i = 0; i < args.length; i++) {
                Object value = args[i];
                if (value == null) {
                    statement.setNull(i + 1, Types.NULL);
                } else if (value instanceof Enum) {
                    statement.setString(i + 1, ((Enum) value).name());
                } else if (value instanceof Date) {//MySQL treatUtilDateAsTimestamp=true
                    statement.setTimestamp(i + 1, new Timestamp(((Date) value).getTime()));
                } else if (value instanceof Instant) {
                    statement.setTimestamp(i + 1, Timestamp.from((Instant) value));
                } else {
                    statement.setObject(i + 1, value);
                }
            }
        } catch (SQLException e) {
            throw new SqlteException(e);
        }
    }

    static String makeInsertSql(String table, String columns) {
//        table.chars().filter(c -> c == ',').count();
        return makeInsertSql(table, columns.split(","));
    }

    static String makeInsertSql(String table, String... columns) {
        return makeInsertSql("INSERT INTO", table, columns);
    }

    static String makeInsertSql(String insert, String table, String... columns) {
        if (insert == null || insert.isEmpty()) {
            insert = "INSERT INTO";
        }
        StringBuilder builder = new StringBuilder(insert);
        builder.append(" ");
        builder.append(table);
        builder.append('(');
        int len = columns.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns[i].trim());
        }
        builder.append(") VALUES (");
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append('?');
        }
        builder.append(')');
        return builder.toString();
    }

    static String makeUpdateSql(String table, String[] columns, String[] pkColumns) {
        StringBuilder builder = new StringBuilder("UPDATE ").append(table);
        builder.append(" SET");
        for (int i = 0, len = columns.length; i < len; i++) {
            String column = columns[i].trim();
            if (i > 0) {
                builder.append(", ");
            } else {
                builder.append(' ');
            }
            builder.append(column);
            builder.append("=?");
        }
        if (pkColumns != null && pkColumns.length > 0) {
            builder.append(" ").append(makeWhereSql(pkColumns));
        }
        return builder.toString();
    }

    private static String makeWhereSql(String... columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("WHERE ");
        for (String column : columns) {
            if (builder.length() > 6) {
                builder.append(" AND ");
            }
            builder.append(column).append(" =?");
        }
        return builder.toString();
    }

    static Object getSerializedValue(Object obj, Field field) {
        try {
            Object value = field.get(obj);
            if (value == null) {
                return null;
            }
            Column column = field.getAnnotation(Column.class);
            // 转成JSON存储
            if (column != null && column.json()) {
                JsonSerializer jsonSerializer = Config.getConfig().getJsonSerializer();
                return jsonSerializer.toJson(value);
            }
            Convert convert = field.getAnnotation(Convert.class);
            if (convert != null) {
                Converter converter = Helper.getConverter(convert.converter());
                return converter.convert(value);
            }
            // 枚举类型转成名称存储
            if (value instanceof Enum) {
                Enum<?> e = (Enum<?>) value;
                return e.name();
            }
            return value;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Class<?>, Supplier<?>> map = new HashMap<>();

    public static <T> Supplier<T> toSupplier(Class<T> clazz) {
        Supplier<?> supplier = map.get(clazz);
        if (supplier == null) {
            synchronized (map) {
                supplier = () -> {
                    try {
                        return clazz.getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                };
                map.put(clazz, supplier);
            }
        }
        return (Supplier<T>) supplier;
    }

    public static final BiConsumer<PreparedStatement, int[]> PRINT_GENERATED_KEYS = (ps, ints) -> {
        try {
            ResultSet keys = ps.getGeneratedKeys();
            while (keys.next()) {
                ResultSetMetaData metaData = keys.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = keys.getObject(i);
                    System.out.println(columnName + "=" + value);
                }
            }
            keys.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

/*    static String[] columns(Field[] fields) {
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            field.getName()
        }
    }*/

}
