package hwp.sqlte;


import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
class Helper {

    static SqlResultSet convert(java.sql.ResultSet rs) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int cols = metaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            columnNames.add(metaData.getColumnLabel(i).toLowerCase().intern());
        }
        List<Row> results = new ArrayList<>();
        while (rs.next()) {
            Row row = new Row();
            for (int i = 1; i <= cols; i++) {
                row.put(columnNames.get(i - 1), rs.getObject(i));
            }
            results.add(row);
        }
        return new SqlResultSet(columnNames, results);
    }

    static void fillStatement(PreparedStatement statement, Object[] args) throws UncheckedSQLException {
        try {
            for (int i = 0; i < args.length; i++) {
                Object value = args[i];
                if (value == null) {
                    statement.setNull(i + 1, Types.NULL);
                } else if (value instanceof Enum) {
                    statement.setString(i + 1, ((Enum) value).name());
                } else if (value instanceof Date) {//MySQL treatUtilDateAsTimestamp=true
                    statement.setTimestamp(i + 1, new Timestamp(((Date) value).getTime()));
                } else {
                    statement.setObject(i + 1, value);
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
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

    static String makeUpdateSql(String table, String... columns) {
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
        return builder.toString();
    }

    static Object getFieldValue(Object obj, Field field) throws IllegalAccessException {
        try {
            Object value = field.get(obj);
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                // 转成JSON存储
                if (column.json()) {
                    JsonSerializer jsonSerializer = Config.getConfig().getJsonSerializer();
                    return jsonSerializer.toJson(value);
                }
            }
 /*           if (column != null) {
                Class<? extends Serializer> serializerClass = column.serializer();
                if (serializerClass != Serializer.class) {
                    Serializer serializer = serializerClass.getDeclaredConstructor().newInstance();
                    return serializer.encode(value);
                }
            }*/
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

/*    static String[] columns(Field[] fields) {
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            field.getName()
        }
    }*/

}
