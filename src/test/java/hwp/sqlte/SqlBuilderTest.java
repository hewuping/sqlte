package hwp.sqlte;

import hwp.sqlte.example.Lte;
import hwp.sqlte.example.StartWith;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
    public void testSql() {
        SqlBuilder sql = new SqlBuilder();
        sql.sql("select *").sql("from").sql("user where username = ?", "zero");
        String expected = "select * from user where username = ?";
        Assert.assertEquals(expected, sql.sql());
        Assert.assertEquals(sql.args(0), "zero");
    }

    @Test
    public void testSelectCount() {
        SqlBuilder sql = new SqlBuilder();
        sql.selectCount("users");
        String expected = "SELECT COUNT(*) AS _COUNT_ FROM users";
        Assert.assertEquals(expected, sql.sql());

        sql = new SqlBuilder();
        sql.selectCount("select * from users");
        expected = "SELECT COUNT(*) AS _COUNT_ FROM ( select * from users ) AS __TABLE__";
        Assert.assertEquals(expected, sql.sql());
    }

    @Test
    public void testWhereNull() {
        SqlBuilder sql = new SqlBuilder();
        sql.select("*").from("users").where((Consumer<Where>) null);
        String expected = "SELECT * FROM users";
        Assert.assertEquals(expected, sql.sql());
        Assert.assertEquals(0, sql.args().length);
    }

    @Test
    public void testWhereConsumer() {
        SqlBuilder sql = new SqlBuilder();
        sql.select("*").from("users").where(where -> {
            // sql
            where.and("age > ?", 10);
            // map
            Map<String, Object> map = new HashMap<>();
            map.put("username", "zero");
            map.put("email", "zero@example.com");
            where.and(map);
            // Condition
            where.and(Condition.in("user_id", new Integer[]{1, 2, 3, 4}));
        });
        String expected = "SELECT * FROM users WHERE age > ? AND email=? AND username=? AND user_id IN (?, ?, ?, ?)";
        Assert.assertEquals(expected, sql.sql());
        Assert.assertEquals(sql.args()[0], 10);
    }

    @Test
    public void testPaging() {
        SqlBuilder sql = new SqlBuilder();
        sql.select("age, COUNT(*)").from("users").paging(1, 10);
        String expected = "SELECT age, COUNT(*) FROM users LIMIT 0, 10";
        Assert.assertEquals(expected, sql.sql());

        sql = new SqlBuilder();
        sql.select("age, COUNT(*)").from("users").paging(3, 15);
        expected = "SELECT age, COUNT(*) FROM users LIMIT 30, 15";
        Assert.assertEquals(expected, sql.sql());

        sql = new SqlBuilder();
        sql.select("age, COUNT(*)").from("users").limit(30, 15);
        expected = "SELECT age, COUNT(*) FROM users LIMIT 30, 15";
        Assert.assertEquals(expected, sql.sql());
    }

    @Test
    public void testGroupBy() {
        SqlBuilder sql = new SqlBuilder();
        sql.select("age, COUNT(*)").from("users").groupBy("age");
        String expected = "SELECT age, COUNT(*) FROM users GROUP BY age";
        Assert.assertEquals(expected, sql.sql());
    }

    @Test
    public void testGroupByHaving() {
        SqlBuilder sql = new SqlBuilder();
        sql.select("age, COUNT(*)").from("users").groupBy("age", having -> {
            having.and("age > 18");
        });
        String expected = "SELECT age, COUNT(*) FROM users GROUP BY age HAVING age > 18";
        Assert.assertEquals(expected, sql.sql());
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
    public void testBetween1() {
        SqlBuilder sql = new SqlBuilder();
        sql.where(where -> {
            where.and(Condition.between("age", Range.of(18, 22)));
        });
        Assert.assertEquals("WHERE (age BETWEEN ? AND ?)", sql.sql());
    }

    @Test
    public void testBetween2() {
        SqlBuilder sql = new SqlBuilder();
        sql.where(where -> {
            where.and(Condition.between("age", Range.of(null, 22)));
        });
        Assert.assertEquals("WHERE age <= ?", sql.sql());
    }

    @Test
    public void testBetween3() {
        SqlBuilder sql = new SqlBuilder();
        sql.where(where -> {
            where.and(Condition.between("age", Range.of(18, null)));
        });
        Assert.assertEquals("WHERE age >= ?", sql.sql());
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

    @Test
    public void testByExample() {
        SqlBuilder builder = new SqlBuilder();
        builder.select(User.class).where(new UserQuery());
        System.out.println(builder);
    }

    private static class UserQuery {
        public Range<Integer> id = Range.of(10, 30);// id BETWEEN 10 AND 30
        @StartWith
        public String name = "z";// name LIKE "z%"
        @Lte
        public Integer deposit = 1000;// deposit <= 1000
        public Integer[] age = new Integer[]{16, 18, 20}; // age IN (16, 18, 20)
        public String status = "Active"; // status = "Active"
    }


    @Test
    public void testCTE_withAs() {
        SqlBuilder builder = new SqlBuilder();
        builder.withAs("ranked_employees", sql -> {
            sql.sql("SELECT name, salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rank FROM employees");
        });
        builder.sql("SELECT name, salary FROM ranked_employees WHERE rank <= 5;");
        String sql = "WITH ranked_employees AS (\n" +
                "    SELECT name, salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rank FROM employees\n" +
                ")\n" +
                "SELECT name, salary FROM ranked_employees WHERE rank <= 5;";
        Assert.assertEquals(sql, builder.sql());
    }

    @Test
    public void testCTE_with() {
        SqlBuilder builder = new SqlBuilder();
        builder.with(cte -> {
            cte.set("customer_orders", sql -> {
                sql.sql("  SELECT customer_id,\n" +
                        "         COUNT(*) AS order_count,\n" +
                        "         SUM(total_amount) AS total_amount,\n" +
                        "         AVG(total_amount) AS avg_amount\n" +
                        "  FROM orders\n" +
                        "  GROUP BY customer_id");
            });
            cte.set("ranked_customers", sql -> {
                sql.sql("  SELECT c.name, o.avg_amount,\n" +
                        "         DENSE_RANK() OVER (ORDER BY o.avg_amount DESC) AS rank\n" +
                        "  FROM customer_orders o\n" +
                        "  JOIN customers c ON o.customer_id = c.id");
            });
        });
        builder.sql("SELECT name, avg_amount FROM ranked_customers WHERE rank <= 5;");

        String expectedSql = "WITH customer_orders AS (\n" +
                "  SELECT customer_id,\n" +
                "         COUNT(*) AS order_count,\n" +
                "         SUM(total_amount) AS total_amount,\n" +
                "         AVG(total_amount) AS avg_amount\n" +
                "  FROM orders\n" +
                "  GROUP BY customer_id\n" +
                "),\n" +
                "ranked_customers AS (\n" +
                "  SELECT c.name, o.avg_amount,\n" +
                "         DENSE_RANK() OVER (ORDER BY o.avg_amount DESC) AS rank\n" +
                "  FROM customer_orders o\n" +
                "  JOIN customers c ON o.customer_id = c.id\n" +
                ")\n" +
                "SELECT name, avg_amount FROM ranked_customers WHERE rank <= 5;";

        Assert.assertEquals(expectedSql, builder.sql());
    }


    @Test
    public void testUnion() {
        SqlBuilder builder = new SqlBuilder();
        builder.select("*").from("users");
        builder.union(sql -> {
            sql.select("*").from("users");
        });
        String expected = "SELECT * FROM users\n" +
                "UNION\n" +
                "SELECT * FROM users";
        Assert.assertEquals(expected, builder.sql());
    }

    @Test
    public void testUnionAll() {
        SqlBuilder builder = new SqlBuilder();
        builder.select("*").from("users");
        builder.unionAll(sql -> {
            sql.select("*").from("users");
        });
        String expected = "SELECT * FROM users\n" +
                "UNION ALL\n" +
                "SELECT * FROM users";
        Assert.assertEquals(expected, builder.sql());
    }
}