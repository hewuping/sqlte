package hwp.sqlte;

import hwp.sqlte.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class WhereTest {

    @Test
    public void testAndIf() {
        Where where = new Where();
        where.and("class =?", "C1");
        where.andIf("name =?", "Sean", StringUtils::isNotEmpty);
        Assert.assertEquals("class =? AND name =?", where.sql());
        Assert.assertEquals("C1", where.args(0));
        Assert.assertEquals("Sean", where.args(1));
    }


    @Test
    public void testOrIf() {
        Where where = new Where();
        where.or("class =?", "C1");
        where.orIf("name =?", "Sean", StringUtils::isNotEmpty);
        Assert.assertEquals("class =? OR name =?", where.sql());
        Assert.assertEquals("C1", where.args(0));
        Assert.assertEquals("Sean", where.args(1));
    }

    @Test
    public void testLike() {
        UserQuery query = new UserQuery();
        query.name = "z";
        query.other = "other";
        Where where = Where.ofExample(query);

        Assert.assertEquals("(name1 LIKE ? OR name2 LIKE ?) AND other = ?", where.sql());
        Assert.assertEquals("[%z%, %z%, other]", where.args().toString());
    }

    @Test
    public void testOfMap() {
        Where where = Where.ofMap(map -> {
            map.put("first_name", "Frank");
            map.put("last_name", "Fu");
        });
        Assert.assertEquals("first_name = ? AND last_name = ?", where.sql());
    }

}
