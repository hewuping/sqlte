package hwp.sqlte;


import java.lang.reflect.Field;
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
 *         Created on 2017/3/20.
 */
class Helper {
    protected static ThreadLocal<SqlConnection> THREAD_LOCAL = new ThreadLocal<>();


    public static SqlResultSet convert(java.sql.ResultSet rs) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int cols = metaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            columnNames.add(metaData.getColumnLabel(i));
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

}
