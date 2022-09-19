package hwp.sqlte;

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

}
