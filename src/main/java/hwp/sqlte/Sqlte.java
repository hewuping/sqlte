package hwp.sqlte;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

/**
 * @author Zero
 *         Created on 2017/3/22.
 */
public class Sqlte {

    public static ScriptEngine getScriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
//        ScriptContext context = new SimpleScriptContext();
//        engine.eval()
        return engine;
    }

}
