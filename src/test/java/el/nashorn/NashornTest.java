package el.nashorn;

import org.junit.Before;
import org.junit.Test;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;
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
        System.out.println(engine.eval(script));
    }

    @Test
    public void testReturn() throws ScriptException {
        IntStream.range(1, 100).forEach(v -> {
            try {
                String script = "print('hello')";
                engine.eval(script);
                System.out.println("hello");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test2() throws ScriptException {
        Bindings user = new SimpleBindings();
        user.put("name", "zero");
        user.put("age", 18);
        user.put("city", "SZ");
        user.put("password", "123456");
        user.put("zero", user);

        IntStream.range(1, 10000).forEach(v -> {
            try {
                String script = "if(1<2)print(zero.name)";
                engine.eval(script,user);
//                if (1 < 2) {
//                    System.out.println(((Bindings)user.get("zero")).get("name"));
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
