package hwp.sqlte;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public class SqlBuilder implements Builder, Sql {
    private List<Object> args = new ArrayList<>();
    private StringBuilder sql = new StringBuilder();

    public SqlBuilder() {

    }

    private static Pattern pattern = Pattern.compile("\\?(\\d+)");

    @Override
    public String sql() {
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            int num = Integer.parseInt(matcher.group(1));
            if (num > 0) {
                int start = matcher.start(0);
                boolean wrap = false;
                if (sql.charAt(start - 1) == '(') {
                    wrap = true;
                }
                StringBuilder rep = new StringBuilder();
                if (!wrap) {
                    rep.append('(');
                }
                for (int i = 0; i < num; i++) {
                    if (i > 0) {
                        rep.append(',');
                    }
                    rep.append('?');
                }
                if (!wrap) {
                    rep.append(')');
                }
                sql.replace(matcher.start(0), matcher.end(0), rep.toString());
                matcher.reset(sql);
            } else {
                //error
                throw new UncheckedException("SQL syntax error: " + sql);
            }
        }
        return sql.toString();
    }

    @Override
    public Object[] args() {
        return this.args.toArray();
    }

    public SqlBuilder sql(String sql) {
        this.sql.append(sql);
        return this;
    }

    public SqlBuilder args(Object... args) {
        if (this.args.size() > 0) {
            this.args.clear();
        }
        addArgs(args);
        return this;
    }

    private void addArgs(Object... args) {
        for (Object arg : args) {
            if (arg.getClass().isArray()) {
                for (int i = 0, len = Array.getLength(arg); i < len; i++) {
                    this.args.add(Array.get(arg, i));
                }
            }
            if (arg instanceof Collection) {
                this.args.addAll((Collection) arg);
            } else {
                this.args.add(arg);
            }
        }
    }


    public SqlBuilder where(Where where) {
        this.sql.append(where);
        this.args.addAll(where.args());
        return this;
    }

    public SqlBuilder where(Consumer<Where> where) {
        Where w = new Where();
        where.accept(w);
        return this.where(w);
    }

    public SqlBuilder limit(int first, int size) {
        this.sql.append(" LIMIT ").append(first).append(",").append(size);
        return this;
    }

    public SqlBuilder limit(int size) {
        this.sql.append(" LIMIT ").append(size);
        return this;
    }

    public SqlBuilder orderBy(Order order) {
        sql.append(order);
        return this;
    }

    public SqlBuilder orderBy(Consumer<Order> consumer) {
        Order order = new Order();
        consumer.accept(order);
        return orderBy(order);
    }


    public SqlBuilder add(String sql, Object... args) {
        this.sql.append(sql);
        this.addArgs(args);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("sql:  ").append(sql()).append("\nargs: ").append(Arrays.toString(args()));
        return builder.toString();
    }

    public static void main(String[] args) {
        SqlBuilder sql = new SqlBuilder();
        sql.add("SELECT * FROM users");
        sql.where(w -> {
            if ("zero".startsWith("z")) {
                w.and("username=?", "zero");
            }
            w.and("password=?", "123456");
            w.and("age in ?2", 1, 2);
            w.and(String.format("age in ?%d", 2), 1, 2);
        });
        sql.orderBy(order -> {
            order.by("username");
            order.desc("age");//
        });
        sql.limit(1, 20);

        System.out.println(sql);
//
//
//        Where where = new Where();
//        where.and("password = ?", "123456");
//        String selectSql = new SqlBuilder().add("SELECT * FROM user").where(where).sql();
//        String selectCount = new SqlBuilder().add("SELECT count(*) FROM user").where(where).sql();
//        System.out.println(selectSql);
//        System.out.println(selectCount);

//        SqlBuilder sql = new SqlBuilder();
//        sql.add("name=? and age in ?", "zero", new int[]{12, 33});
//        System.out.println(sql.sql());
//        System.out.println(Arrays.toString(sql.args()));
    }


    @Override
    public Sql build() {
        return new SimpleSql(sql(), args());
    }

}
