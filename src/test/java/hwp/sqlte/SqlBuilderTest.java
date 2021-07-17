package hwp.sqlte;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Zero
 * Created on 2018/10/9.
 */
public class SqlBuilderTest {

    @Test
    public void testSelectSql() {
        SqlBuilder sql = new SqlBuilder();
        sql.from("users");
        sql.where(where -> {
            if ("zero".startsWith("z")) {
                where.and("username=?", "zero");
            }
            where.and(Condition.startWith("username", "Z"));
            where.and("password=?", "123456");
            where.and(Condition.in("age", 12, 13, 15, 17));
        });
        sql.groupBy("age", having -> {
            having.and("age < ?", 18);
            having.andOr(Condition.eq("username", "Zero"), Condition.eq("username", "Frank"));
        });
        sql.orderBy(order -> {
            order.asc("username");
            order.desc("age");
        });
        sql.limit(1, 20);
        String expected = "SELECT * FROM users WHERE username=? AND username LIKE ? AND password=? AND age IN (?, ?, ?, ?) GROUP BY age HAVING age < ? AND (username = ? OR username = ?) ORDER BY username ASC, age DESC LIMIT 1, 20";
        Assert.assertEquals(expected, sql.sql());
//        System.out.println(sql);
    }

    @Test
    public void testSelectSqlArray() {
        SqlBuilder sql = new SqlBuilder();
        sql.from("users");
        sql.where(where -> {
            where.and(Condition.in("age", new Integer[]{12, 13, 15, 17}, 6));
            where.and(Condition.in("username", "Zero,Frank".split(",")));
        });
        Assert.assertEquals("SELECT * FROM users WHERE age IN (?, ?, ?, ?, ?) AND username IN (?, ?)", sql.sql());
        Assert.assertEquals("[12, 13, 15, 17, 6, Zero, Frank]", Arrays.toString(sql.args()));
    }

    @Test
    public void testUpdateSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.update("users", "age,username", 12, "zero")
                .where(where -> where.and(Condition.eq("id", 123456)));
        Assert.assertEquals("UPDATE users SET age=?, username=? WHERE id = ?", builder.sql());
        System.out.println(builder);
        Assert.assertEquals(3, builder.args().length);
    }

    @Test
    public void testDeleteSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.delete("users").where(where -> where.and(Condition.eq("id", 123456)));
        Assert.assertEquals("DELETE FROM users WHERE id = ?", builder.sql());
        Assert.assertEquals(1, builder.args().length);
    }
}