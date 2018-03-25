package hwp.sqlte;


import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author Zero
 *         Created on 2017/3/20.
 */
public class Helper {

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
                row.put(columnNames.get(i-1), rs.getObject(i));
            }
            results.add(row);
        }
        return new SqlResultSet(columnNames, results);
    }

    public static void fillStatement(PreparedStatement statement, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            if (value != null) {
                if (value instanceof String) {
                    statement.setString(i + 1, Objects.toString(value));
                } else {
                    statement.setObject(i + 1, value);
                }
            } else {
                statement.setNull(i + 1, Types.NULL);
            }
        }
    }

    public static Map<String,Object> beanToArgs(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = obj.getClass().getFields();
        for (Field field : fields) {
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

}
