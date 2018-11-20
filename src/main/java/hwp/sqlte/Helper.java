package hwp.sqlte;


import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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
                } else {
                    if (value instanceof Enum) {
                        statement.setString(i + 1, ((Enum) value).name());
                    } else {
                        statement.setObject(i + 1, value);
                    }
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
        StringBuilder builder = new StringBuilder("INSERT INTO ").append(table);
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

}
