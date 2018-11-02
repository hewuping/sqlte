package hwp.sqlte;

//import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

//import com.mysql.cj.jdbc.MysqlDataSource;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * @author Zero
 * Created by Zero on 2017/6/17 0017.
 */
public class SqlProviderTest {

    SqlProvider sqlProvider = Config.getConfig().getSqlProvider();

    @Test
    public void test1() {
        String sql1 = sqlProvider.getSql("all");
        String sql2 = sqlProvider.getSql("user.login");
        String sql3 = sqlProvider.getSql("multi-line-sql");
        Assert.assertEquals("SELECT * FROM  users", sql1);
        Assert.assertEquals("SELECT * FROM users WHERE username=? and password=?", sql2);
        Assert.assertEquals("SELECT\n" +
                "*\n" +
                "FROM\n" +
                "orders\n" +
                "LEFT JOIN users ON orders.user_id = users.id\n" +
                "WHERE\n" +
                "users.username = ?", sql3);
    }

    @Test
    public void test2() {
        try {
            sqlProvider.getSql("user2.login");
        } catch (Exception e) {
            Assert.assertSame(FileNotFoundException.class, e.getCause().getClass());
        }
    }

}