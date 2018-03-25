package hwp.sqlte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public class Update {


    public static String make(String table, String columnsStr, String where, Object... values) {
        String[] columns = columnsStr.split(",");
        StringBuilder builder = new StringBuilder("UPDATE ").append(table);
        builder.append(" SET ");
        for (int i = 0; i < columns.length; ) {
            builder.append(columns[i]).append("=?");
            if (++i < columns.length) {
                builder.append(',');
            }
        }
        if (where != null) {
            builder.append(" WHERE ");
            builder.append(where);
        }
        return builder.toString();
    }

    public Sql build() {
        return null;

    }

    public static void main(String[] args) throws Exception {
 /*       Sql.runOnTx(conn -> {
            return conn.update(sql -> {
                sql.table("users");
                sql.set("name", "value");
                sql.where("");
            });

            conn.execut(user, sb -> {
                sb.append("select * from user where username=?",user.username);
            });
        });*/
    }


}
