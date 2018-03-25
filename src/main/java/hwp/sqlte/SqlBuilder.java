package hwp.sqlte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class SqlBuilder implements Sql {
    private List<Object> args = new ArrayList<>();
    private StringBuilder sql = new StringBuilder();

    private String errmsg;

    public SqlBuilder() {

    }

    @Override
    public String sql() {
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
        Collections.addAll(this.args, args);
        return this;
    }


    public SqlBuilder where(Consumer<Where> where) {
        Where w = new Where();
        where.accept(w);
        this.sql.append(w);
        return this;
    }

    public SqlBuilder order(Where where) {
        return this;
    }

    public SqlBuilder limit(int offset, int size) {
        return this;
    }


    public class Where {
        private StringBuilder builder = new StringBuilder();

        public Where add(String sql, Object... args) {
            return add(true, sql, args);
        }

        public Where add(boolean filter, String sql, Object... args) {
            if (filter) {
                if (builder.length() == 0) {
                    builder.append(" WHERE");
                }
                builder.append(" ");
                if (sql.contains("(?)") && args.length > 1) {
                    //统计问号个数
//                long count = sql.chars().filter(c -> c == '?').count();
                    StringBuilder b = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        if (b.length() > 0) {
                            b.append(",");
                        }
                        b.append("?");
                    }
                    b.insert(0, "(");
                    b.append(")");
                    sql = sql.replace("(?)", b);
                }
                builder.append(sql);
                Collections.addAll(SqlBuilder.this.args, args);
            }
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }

    static Pattern pattern = Pattern.compile(":(\\w+)");

    public static String normalizeSql(String sql) {
        if (sql.contains(":")) {
            return pattern.matcher(sql).replaceAll("?");
        }
        return sql;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("sql:  ").append(sql()).append("\nargs: ").append(Arrays.toString(args()));
        return builder.toString();
    }

    public static void main(String[] args) {
        SqlBuilder sql = new SqlBuilder();
        sql.add("select * from users");
        sql.where(w -> {
            if ("zero".startsWith("z")) {
                w.add("username=?", "zero");
            }
            w.add("and password=?", "123456");
            w.add("and age in ?", new Object[]{1, 2});
        });

//        sql.where(w -> {
//            w.add("username = ?", "zero");
//            w.add("and age in (?)", 1, 2, 3);
//        });
        System.out.println(sql);
    }

    public void add(String sql, Object... args) {
        this.sql.append(sql);
        Collections.addAll(this.args, args);
    }

}
