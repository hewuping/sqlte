package hwp.sqlte;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Zero
 *         Created on 2018/10/9.
 */
public class SqlBuilderTest {

    @Test
    public void testBuild() throws Exception {
        SqlBuilder sql = new SqlBuilder();
        sql.add("SELECT * FROM users");
        sql.where(w -> {
            if ("zero".startsWith("z")) {
                w.and("username=?", "zero");
            }
            w.and("password=?", "123456");
            w.and("age in ?2", 1, 2);//w.and(String.format("age in ?%d", 2), 1, 2);
        });
        sql.orderBy(order -> {
            order.by("username");
            order.desc("age");//
        });
        sql.limit(1, 20);
        Assert.assertEquals(sql.sql(), "SELECT * FROM users WHERE username=? AND  password=? AND  age in (?,?) ORDER BY username ASC, age DESC LIMIT 1,20");
        System.out.println(sql);
    }
}