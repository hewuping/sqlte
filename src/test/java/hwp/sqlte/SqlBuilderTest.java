package hwp.sqlte;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Zero
 * Created on 2018/10/9.
 */
public class SqlBuilderTest {

    @Test
    public void testSelectSql() {
        SqlBuilder sql = new SqlBuilder();
        sql.select("users");
        sql.where(where -> {
            if ("zero".startsWith("z")) {
                where.and("username=?", "zero");
            }
            where.and(Condition.startWith("username", "Z"));
            where.and("password=?", "123456");
            where.and(Condition.in("age", 12, 13, 15, 17));
        });
        sql.groupBy(group -> {
            group.by("age");
        }, having -> {
            having.and("age < ?", 18);
            having.and(Condition.neq("username", "Zero"), Condition.neq("username", "Frank"));
        });
        sql.orderBy(order -> {
            order.by("username");//eq: order.asc("username"); order.asc("username","ASC")
            order.desc("age");
        });
        sql.limit(1, 20);
//        Assert.assertEquals(sql.sql(), "SELECT * FROM users WHERE username=? AND  password=? AND  age in (?,?) ORDER BY username ASC, age DESC LIMIT 1,20");
        System.out.println(sql);
    }

    @Test
    public void testUpdateSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.update("users", "age,username", 12, "zero")
                .where(where -> where.and(Condition.eq("id", 123456)));
        Assert.assertEquals(builder.sql(), "UPDATE users SET age=?, username=? WHERE id = ?");
        Assert.assertEquals(builder.args().length, 3);
        System.out.println(builder);
    }

    @Test
    public void testDeleteSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.delete("users").where(where -> where.and(Condition.eq("id", 123456)));
        Assert.assertEquals(builder.sql(), " DELETE FROM users WHERE id = ?");
        Assert.assertEquals(builder.args().length, 3);
        System.out.println(builder);
    }
}