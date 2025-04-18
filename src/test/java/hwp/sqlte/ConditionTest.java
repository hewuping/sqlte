package hwp.sqlte;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Zero
 * Created on 2021/2/19.
 */
public class ConditionTest {

    @Test
    public void testIsNull() {
        Condition con = Condition.isNull("name");
        Assert.assertEquals("name IS NULL", con.sql());
    }

    @Test
    public void testIsNotNull() {
        Condition con = Condition.isNotNull("name");
        Assert.assertEquals("name IS NOT NULL", con.sql());
    }

    @Test
    public void testEq() {
        Condition con = Condition.eq("name", "Zero");
        Assert.assertEquals("name = ?", con.sql());
        Assert.assertEquals("[Zero]", Arrays.toString(con.args()));
    }

    @Test
    public void testNeq() {
        Condition con = Condition.neq("name", "Zero");
        Assert.assertEquals("name <> ?", con.sql());
        Assert.assertEquals("[Zero]", Arrays.toString(con.args()));
    }

    @Test
    public void testLike() {
        Condition con = Condition.like("name", "%Zero");
        Assert.assertEquals("name LIKE ?", con.sql());
        Assert.assertEquals("[%Zero]", Arrays.toString(con.args()));
    }

    @Test
    public void testStartWith() {
        Condition con = Condition.startWith("name", "Ze");
        Assert.assertEquals("name LIKE ?", con.sql());
        Assert.assertEquals("[Ze%]", Arrays.toString(con.args()));
    }

    @Test
    public void testEndWith() {
        Condition con = Condition.endWith("name", "Ze");
        Assert.assertEquals("name LIKE ?", con.sql());
        Assert.assertEquals("[%Ze]", Arrays.toString(con.args()));
    }

    @Test
    public void testGt() {
        Condition con = Condition.gt("age", 12);
        Assert.assertEquals("age > ?", con.sql());
        Assert.assertEquals("[12]", Arrays.toString(con.args()));
    }

    @Test
    public void testGte() {
        Condition con = Condition.gte("age", 12);
        Assert.assertEquals("age >= ?", con.sql());
        Assert.assertEquals("[12]", Arrays.toString(con.args()));
    }

    @Test
    public void testLt() {
        Condition con = Condition.lt("age", 12);
        Assert.assertEquals("age < ?", con.sql());
        Assert.assertEquals("[12]", Arrays.toString(con.args()));
    }

    @Test
    public void testLte() {
        Condition con = Condition.lte("age", 12);
        Assert.assertEquals("age <= ?", con.sql());
        Assert.assertEquals("[12]", Arrays.toString(con.args()));
    }

    @Test
    public void testBetween() {
        Condition con = Condition.between("age", 10, 20);
        Assert.assertEquals("age BETWEEN ? AND ?", con.sql());
        Assert.assertEquals("[10, 20]", Arrays.toString(con.args()));
    }

    @Test
    public void testBetween2() {
        Condition con = Condition.between("age", null, 20);
        Assert.assertEquals("age <= ?", con.sql());
        Assert.assertEquals("[20]", Arrays.toString(con.args()));
    }

    @Test
    public void testBetween3() {
        Condition con = Condition.between("age", 10, null);
        Assert.assertEquals("age >= ?", con.sql());
        Assert.assertEquals("[10]", Arrays.toString(con.args()));
    }

    @Test
    public void testBetweenRange() {
        Condition con = Condition.between("age", Range.of(10, 20));
        Assert.assertEquals("age BETWEEN ? AND ?", con.sql());
        Assert.assertEquals("[10, 20]", Arrays.toString(con.args()));
    }

    @Test
    public void testIn() {
        String sqlExpected = "name IN (?, ?)";
        String argsExpected = "[Zero, Frank]";
        Condition con = Condition.in("name", "Zero", "Frank");
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));

        con = Condition.in("name", "Zero,Frank".split(","));
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));

        con = Condition.in("name", new String[]{"Zero", "Frank"});
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));

        con = Condition.in("name", new String[]{"Zero"}, "Frank");
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));


        con = Condition.in("name", Arrays.asList("Zero", "Frank"));
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));

        con = Condition.in("name", Arrays.asList("Zero"), "Frank");
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));

        con = Condition.in("name", Arrays.asList("Zero", "Frank").stream());
        Assert.assertEquals(sqlExpected, con.sql());
        Assert.assertEquals(argsExpected, Arrays.toString(con.args()));
    }

    @Test
    public void testNotIn() {
        String sqlExpected = "name NOT IN (?, ?)";
        Condition con = Condition.notIn("name", Arrays.asList("Zero", "Frank"));
        Assert.assertEquals(sqlExpected, con.sql());
    }

    @Test
    public void testInEmpty() {
        try {
            Condition.in("name", new Object[0]);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("查询条件 name 列的 IN 参数值不能为空", e.getMessage());
        }
    }

}
