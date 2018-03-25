package el;

import javax.script.*;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Zero
 *         Created on 2017/3/22.
 */
public class Main {

    public static <T> T map(ResultSet resultSet, T t) throws Exception {
        Field[] fields = t.getClass().getFields();//只映射public字段，public字段必须有
        if (resultSet.next()) {
            for (Field field : fields) {
                //NameConverter
                Object value = resultSet.getObject(field.getName(), field.getType());
                field.set(t,value);
            }
        }
        return null;
    }


    public static <T> T flatMap(T t) {
        Field[] fields = t.getClass().getFields();

        return null;
    }

    public static class Foo {
        private String name;
        public Map<String, String> map = new HashMap<>();

        {
            map.put("zero", "haha");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) throws ScriptException {
        Foo foo = new Foo();
        foo.setName("test");
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.put("foo", foo);

        Object eval = engine.eval("foo.name");
        System.out.println(eval);
        System.out.println(engine.eval("foo.map.zero"));
//        System.out.println(engine.eval("foo2"));//ScriptException
        System.out.println(engine.eval("foo.ss"));//ScriptException

        Bindings bindings = new SimpleBindings();
        bindings.put("time", "2012");
//        System.out.println(engine.eval("foo.name",bindings));
        System.out.println(engine.eval("time", bindings));
        System.out.println(engine.eval("time", bindings));


        String h1 = "hello";
        String h2 = "hello";
        String h3 = hello();
        String h4 = hello();
        System.out.println(h1 == h2);
        System.out.println(h1 == h3);
        System.out.println(h3 == h4);
    }

    public static String hello() {
        return "hello";
    }
}
