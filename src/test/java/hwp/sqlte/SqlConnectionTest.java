package hwp.sqlte;

//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import hwp.sqlte.example.User;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Zero
 *         Created by Zero on 2017/6/17 0017.
 */
public class SqlConnectionTest {
    @Before
    public void setUp() throws Exception {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setURL("jdbc:mysql://localhost:3306/test?serverTimezone=UTC&characterEncoding=utf-8");
        ds.setUser("root");
        ds.getConnection().close();
        Sql.DATA_SOURCE_RESOURCE.set(ds);
        Sql.setDatasource("default", ds);
    }

    @Test
    public void query() throws Exception {
        Sql.runOnTx(conn -> {
            SqlResultSet resultSet = conn.query(sb -> {
                sb.add("select * from auth_user2");
                sb.where(where -> {
                    where.add("username=?", "Frank");
                });
                System.out.println(sb.sql());
            });
            System.out.println(resultSet);
            return "";
        });
    }


    @Test
    public void insert() throws Exception {
        Sql.runOnTx(conn -> {
//            conn.insert("insert into auth_user(username,password,password_salt) values(?,?,?)", "zero", "123456", "xxx");
            conn.insert(Insert.make("auth_user2", "username,password,password_salt"), "may", "123456", "xxx");
            return "";
        });
    }

    @Test
    public void insert2() throws Exception {
        Sql.runOnTx(conn -> {
            conn.insert(new User("Frank", "123456", "xswenli"), "auth_user2");
            SqlResultSet rows = conn.query("select * from auth_user2 where username =? limit 10", "Frank");
            List<User> users = rows.map(row -> row.convert(new User()));
//            List<User> users = rows.flatMap(User::new);
            System.out.println(users.size());
            return "";
        });
    }


    @Test
    public void update() throws Exception {
        Sql.runOnTx(conn -> {
            conn.update(new User("Frank", "123456", "xswenli"), "auth_user2");
            SqlResultSet rows = conn.query("select * from auth_user2 where username =? limit 10", "Frank");
            List<User> users = rows.map(User::new);
            System.out.println(users.size());
            return "";
        });
    }


}