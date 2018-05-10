package hwp.sqlte;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public class Insert {

    public static String make(String table, String columns) {
        return make(table, columns.split(","));
    }

    public static String make(String table, String... columns) {
        StringBuilder builder = new StringBuilder("INSERT INTO ").append(table);
        builder.append('(');
        int len = columns.length;
        for (int i = 0; i < len; ) {
            builder.append(columns[i].trim());
            if (++i < len) {
                builder.append(',');
            }
        }
        builder.append(") VALUES (");
        for (int i = 0; i < len; ) {
            builder.append('?');
            if (++i < len) {
                builder.append(',');
            }
        }
        builder.append(')');
        return builder.toString();
    }


    public static void main(String[] args) throws SQLException {
/*        StandardSql standardSql = new StandardSql("insert into user(username,password) values(?,?)");
        standardSql.args("Zero", "123456");
        Insert insert = new Insert(standardSql);
        User user = new User();
        insert.execute(true).handleResult(rs -> {
            Row row = rs.first();
            if (row != null) {
//              user.id= row.get("id");
            }
        });*/

        Pattern pattern = Pattern.compile(":(\\w+)");
        Matcher matcher = pattern.matcher("insert user values(:username,:password)");
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }
        System.out.println("INSERT INTO user VALUES(:username,:password)".replaceAll(":(\\w+)", "?"));

        System.out.println(Insert.make("user", "username", "password", "slat"));
        System.out.println(Insert.make("user", "username,password,slat"));

    }


}
