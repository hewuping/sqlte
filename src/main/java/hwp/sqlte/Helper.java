package hwp.sqlte;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zero
 * Created on 2017/3/20.
 */
public class Helper {
    protected static ThreadLocal<SqlConnection> THREAD_LOCAL = new ThreadLocal<>();


    public static SqlResultSet convert(java.sql.ResultSet rs) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int cols = metaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            columnNames.add(metaData.getColumnLabel(i).intern());
        }
        List<Row> results = new ArrayList<>();
        while (rs.next()) {
            Row row = new Row();
            for (int i = 1; i <= cols; i++) {
                row.put(columnNames.get(i - 1).intern(), rs.getObject(i));
            }
            results.add(row);
        }
        return new SqlResultSet(columnNames, results);
    }

    public static void fillStatement(PreparedStatement statement, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            if (value == null) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
        }
    }

    public static String makeInsertSql(String table, String columns) {
//        table.chars().filter(c -> c == ',').count();
        return makeInsertSql(table, columns.split(","));
    }

    public static String makeInsertSql(String table, String... columns) {
        StringBuilder builder = new StringBuilder("INSERT INTO ").append(table);
        builder.append('(');
        int len = columns.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(columns[i].trim());
        }
        builder.append(") VALUES (");
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('?');
        }
        builder.append(')');
        return builder.toString();
    }

    public static String makeUpdateSql(String table, String... columns) {
        StringBuilder builder = new StringBuilder("UPDATE ").append(table);
        builder.append(" SET");
        for (int i = 0, len = columns.length; i < len; i++) {
            String column = columns[i];
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


    public static Map<String, Object> beanToArgs(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getFields();
        for (Field field : fields) {
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }


    public static boolean hasPublicField(Class clazz, String name) {
        for (Field field : clazz.getFields()) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static Map<Field, String> fieldColumnMap = new HashMap<>();

    public static String getColumnName(Field field) {
        String columnName = fieldColumnMap.get(field);
        if (columnName != null) return columnName;
        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            String fieldName = field.getName();
            StringBuilder builder = new StringBuilder(fieldName.length());
            for (int i = 0, len = fieldName.length(); i < len; i++) {
                if (Character.isUpperCase(fieldName.charAt(i))) {
                    builder.append('_').append(fieldName.charAt(i));
                } else {
                    builder.append(fieldName.charAt(i));
                }
            }
            columnName = builder.toString();
        } else {
            columnName = column.name();
        }
        fieldColumnMap.put(field, columnName);
        return columnName;
    }

    private static Map<Class, Map<String, Field>> columnFieldMap = new HashMap<>();

    public static Field getField(Class<?> clazz, String columnName) {
        Map<String, Field> map = columnFieldMap.get(clazz);
        if (map == null) {
            map = new HashMap<>();
            for (Field field : clazz.getFields()) {
                if (isPublicField(field)) {
                    map.put(field.getName(), field);
                    map.put(Helper.getColumnName(field), field);
                }
            }
            columnFieldMap.put(clazz, map);
        }
        return map.get(columnName);
    }

    private static boolean isPublicField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }

}
