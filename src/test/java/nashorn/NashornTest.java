package nashorn;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.script.*;
import java.util.stream.IntStream;

/**
 * @author Zero
 *         Created on 2017/3/22.
 */
public class NashornTest {


    ScriptEngine engine;

    @Before
    public void bf() {
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");
    }


    @Test
    public void testPrint() throws ScriptException {
        String script = "print('hello')";
        engine.eval(script);
    }


    @Test
    public void testEL() throws ScriptException {
        Bindings user = new SimpleBindings();
        user.put("name", "zero");
        user.put("age", 18);
        user.put("city", "SZ");
        user.put("password", "123456");
        user.put("zero", user);
        String script = "if(1<2){zero.name}";
        Object name = engine.eval(script, user);
        Assert.assertEquals("zero", name);
    }

    @Test
    public void testELTime() throws ScriptException {
        Bindings user = new SimpleBindings();
        user.put("name", "zero");
        user.put("age", 18);
        user.put("city", "SZ");
        user.put("password", "123456");

        IntStream.range(1, 10000).forEach(v -> {
            try {
                engine.eval("name", user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void testJavaTime() throws ScriptException {
        Bindings user = new SimpleBindings();
        user.put("name", "zero");
        user.put("age", 18);
        user.put("city", "SZ");
        user.put("password", "123456");

        IntStream.range(1, 10000).forEach(v -> {
            user.get("name");
        });
    }

}
