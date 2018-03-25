package hwp.sqlte.gg;

import hwp.sqlte.Builder;
import hwp.sqlte.Order;
import hwp.sqlte.Sql;

import javax.script.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class TemplateSql implements Sql {
    //StandardSql,SqlTemplate
    private StringBuilder sqlBuilder;
    private String sql;
    private List<Object> args = new ArrayList<>();

    private Bindings data = new SimpleBindings();

    public TemplateSql(String sql) {
        this.sqlBuilder = new StringBuilder(sql);
    }

    @Override
    public String sql() {
        if (sql == null) {
            try {
                sql = normalizeSql();
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        return this.sql;
    }

    @Override
    public Object[] args() {
        return this.args.toArray();
    }

    public TemplateSql addVar(String name, Object value) {
        data.put(name, value);
        return this;
    }

    public TemplateSql setDefVar(Object value) {
        data.put("_", value);
        return this;
    }


    public TemplateSql where(Builder<Where> builder) {
        Where obj = new Where(this);
        builder.build(obj);
        this.sqlBuilder.append(obj);
        return this;
    }

    public TemplateSql order(Builder<Order> builder) {
        Order obj = new Order();
        builder.build(obj);
        this.sqlBuilder.append(obj);
        return this;
    }

    public TemplateSql order(Order order) {
        return this;
    }

    public TemplateSql limit(int offset, int size) {
        return this;
    }


    public static class Where {
        private TemplateSql sql;
        private StringBuilder builder = new StringBuilder();

        public Where(TemplateSql sql) {
            this.sql = sql;
        }

        public Where add(String template) {
            return add(template, true);
        }

        public Where add(String template, boolean exp) {
            if (exp) {
                if (builder.length() == 0) {
                    builder.append(" where");
                }
                builder.append(" ").append(template);
            }
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }

    }

    //    static Pattern pattern = Pattern.compile(":([\\w]+)");
    static Pattern pattern = Pattern.compile("(:|\\$)([A-z_\\d\\.]+)");


    private String normalizeSql() throws ScriptException {
        if (sqlBuilder.indexOf(":") > -1) {
            ScriptEngine engine = getScriptEngine();
            boolean hasDef = data.containsKey("_");
            Matcher matcher = pattern.matcher(sqlBuilder);
            while (matcher.find()) {
                String ph = matcher.group(1);
                String name = matcher.group(2);
                int firstDot = name.indexOf('.');
                String base = firstDot == -1 ? name : name.substring(0, firstDot);
                Object val = null;
                if (data.containsKey(base)) {
                    val = engine.eval(name, data);
                } else if (hasDef) {
                    val = engine.eval("_." + name, data);
                }
                if ("$".equals(ph)) {
                    Objects.requireNonNull(val, "Can't found property:" + name);
                    sqlBuilder.replace(matcher.start(1), matcher.end(1), Objects.toString(val));
                } else {//:
                    this.args.add(val);
                }
            }
            return pattern.matcher(sqlBuilder).replaceAll("?");
        }
        return sqlBuilder.toString();
    }

    public static ScriptEngine getScriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
//        ScriptContext context = new SimpleScriptContext();
//        engine.eval()
        return engine;
    }

    public static void main(String[] args) {

        Map<String, Object> user = new HashMap<>();
        user.put("name", "zero");
        user.put("age", 18);
        user.put("city", "SZ");
        user.put("password", "123456");
        user.put("ages", new int[]{1, 2, 3});


        TemplateSql sql = new TemplateSql("select * from user");
        sql.addVar("user", user);
        sql.where(w -> {
            w.add("username=:user.name");
            w.add("and password=:user.password");
            w.add("and age in :ages");
        });

        System.out.println(sql.sql());
        System.out.println(Arrays.toString(sql.args()));


        TemplateSql sql2 = new TemplateSql("select * from user");
        sql2.setDefVar(user);
        sql2.where(w -> {
            w.add("username=:name");
            w.add("and password=:password");
            w.add("and password=:pp_22d");
        });
        System.out.println(sql.sql());
        System.out.println(Arrays.toString(sql.args()));

    }

}
