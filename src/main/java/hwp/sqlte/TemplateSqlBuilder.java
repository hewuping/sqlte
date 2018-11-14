package hwp.sqlte;

import javax.script.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class TemplateSqlBuilder implements Builder {
    private static Pattern pattern = Pattern.compile("(:|\\$)([A-z_\\d\\.]+)");

    //StandardSql,SqlTemplate
    private StringBuilder sqlBuilder;
    private List<Object> args = new ArrayList<>();

    private Bindings data = new SimpleBindings();

    public TemplateSqlBuilder(String sql) {
        this.sqlBuilder = new StringBuilder(sql);
    }


    public TemplateSqlBuilder addVar(String name, Object value) {
        data.put(name, value);
        return this;
    }

    public TemplateSqlBuilder setRootVar(Object value) {
        data.put("_", value);
        return this;
    }


    public TemplateSqlBuilder where(Consumer<Where> builder) {
        Where obj = new Where();
        builder.accept(obj);
        this.sqlBuilder.append(obj);
        return this;
    }

    public TemplateSqlBuilder orderBy(Order order) {
        sqlBuilder.append(order);
        return this;
    }

    public TemplateSqlBuilder orderBy(Consumer<Order> consumer) {
        Order order = new Order();
        consumer.accept(order);
        return orderBy(order);
    }

    public TemplateSqlBuilder limit(int first, int size) {
        this.sqlBuilder.append(" LIMIT ").append(first).append(",").append(size);
        return this;
    }

    public TemplateSqlBuilder limit(int size) {
        this.sqlBuilder.append(" LIMIT ").append(size);
        return this;
    }

    @Override
    public Sql build() {
        try {
            if (sqlBuilder.indexOf(":") > -1) {
                ScriptEngine engine = getScriptEngine();
                boolean hasDef = data.containsKey("_");
                Matcher matcher = pattern.matcher(sqlBuilder);
                while (matcher.find()) {
                    String name = matcher.group(2);
                    int firstDot = name.indexOf('.');
                    String base = firstDot == -1 ? name : name.substring(0, firstDot);
                    Object val = null;
                    if (data.containsKey(base)) {
                        val = engine.eval(name, data);
                    } else if (hasDef) {
                        val = engine.eval("_." + name, data);
                    }
                    if (val != null && (val.getClass().isArray() || val instanceof Collection)) {
                        int start = matcher.start();
                        boolean wrap = false;
                        if (sqlBuilder.charAt(start - 1) == '(') {
                            wrap = true;
                        }
                        StringBuilder rep = new StringBuilder();
                        if (!wrap) {
                            rep.append('(');
                        }
                        if (val.getClass().isArray()) {
                            for (int i = 0, len = Array.getLength(val); i < len; i++) {
                                if (i > 0) {
                                    rep.append(',');
                                }
                                rep.append('?');
                                this.args.add(Array.get(val, i));
                            }
                        } else {
                            Collection c = (Collection) val;
                            int index = 0;
                            for (Object obj : c) {
                                if (index++ > 0) {
                                    rep.append(',');
                                }
                                rep.append('?');
                                this.args.add(obj);
                            }
                        }

                        if (!wrap) {
                            rep.append(')');
                        }
                        sqlBuilder.replace(matcher.start(), matcher.end(), rep.toString());
                        matcher.reset(sqlBuilder);
                    } else {
                        sqlBuilder.replace(matcher.start(), matcher.end(), "?");
                        this.args.add(val);
                    }
                }
                return new SimpleSql(this.sqlBuilder.toString(), this.args);
            }
            return new SimpleSql(this.sqlBuilder.toString());
        } catch (ScriptException e) {
            throw new UncheckedException("Script error: " + e.getMessage());
        }
    }


    public static class Where {
        private StringBuilder whereBuilder = new StringBuilder();

        public String sql() {
            return whereBuilder.toString();
        }

        public Where add(String template) {
            return add(template, true);
        }

        public Where add(String template, boolean exp) {
            return add(null, template, exp);
        }

        public Where and(String template) {
            return add("AND", template, true);
        }

        public Where and(String template, boolean exp) {
            return add("AND", template, exp);
        }

        public Where or(String template) {
            return add("OR", template, true);
        }

        public Where or(String template, boolean exp) {
            return add("OR", template, exp);
        }

        private Where add(String operator, String template, boolean exp) {
            if (exp) {
                if (whereBuilder.length() == 0) {
                    whereBuilder.append(" WHERE");
                }
                if (operator != null && whereBuilder.length() > 8) {
                    whereBuilder.append(" ");
                    whereBuilder.append(operator);
                }
                whereBuilder.append(" ");
                whereBuilder.append(template);
            }
            return this;
        }

        @Override
        public String toString() {
            return sql();
        }

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

        TemplateSqlBuilder builder = new TemplateSqlBuilder("select * from user");
        builder.addVar("user", user);
        builder.addVar("ages", new int[]{1, 2, 3, 9});
        builder.where(w -> {
            w.add("username=:user.name");
            w.add("and password=:user.password");
            w.and("age in (:ages)");
            w.or("age in (:ages)");
        });
        Sql sql = builder.build();
        System.out.println(sql.sql());
        System.out.println(Arrays.toString(sql.args()));

        System.out.println(sql.sql());
        System.out.println(Arrays.toString(sql.args()));

    }

}
