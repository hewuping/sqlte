package hwp.sqlte.util;

import hwp.sqlte.Range;
import hwp.sqlte.util.QueryRequest;
import org.junit.Assert;
import org.junit.Test;

public class QueryRequetTest {
    @Test
    public void test() {
        String json = "{\n" +
                "       \"query\": {\n" +
                "           \"name\": \"Zero\",\n" +
                "           \"age\": {\"start\":18,\"end\":20}\n" +
                "       },\n" +
                "     \"sort\": {\n" +
                "           \"name\": \"DESC\",\n" +
                "           \"age\": \"ASC\"\n" +
                "   },\n" +
                "   \"from\": 5,\n" +
                "   \"size\": 20\n" +
                "  }";
        QueryRequest request = QueryRequest.fromJson(json);
        UserQuery query = request.getQueryAs(UserQuery.class);
        Assert.assertEquals("Zero", query.name);
        Assert.assertNotNull(query.age);
    }

    @Test
    public void testQueryString() {
        String queryString = "name=Frank&age=18~20&sort=name:desc,age:asc&from=5&size=20";
        QueryRequest request = QueryRequest.fromQueryString(queryString);
        UserQuery query = request.getQueryAs(UserQuery.class);
        Assert.assertEquals("Frank", query.name);
        Assert.assertNotNull(query.age);
    }


    public static class UserQuery {
        public String name;
        public Range<Integer> age;
    }
}
