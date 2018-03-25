package hwp.sqlte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public class StandardSql implements Sql {
    //StandardSql,SqlTemplate
    private String sql;
    private String normalizeSql;
    private List<NameParameter> parameters;
    private List<Object> args = new ArrayList<>();

    public StandardSql(String sql) {
        this.sql = sql;
        if (sql.contains(":")) {
            this.normalizeSql = normalizeSql(sql);
            this.parameters = findNameParameters(this.sql);
        } else {
            this.normalizeSql = sql;
        }
    }

    @Override
    public String sql() {
        return normalizeSql;
    }

    @Override
    public Object[] args() {
        return this.args.toArray();
    }


    public StandardSql args(Object... args) {
        if (this.args.size() > 0) {
            this.args.clear();
        }
        Collections.addAll(this.args, args);
        return this;
    }

    private StandardSql addWhere(Where where) {
        return this;
    }

    public StandardSql where(Consumer<Where> where) {
        Where w = new Where(this);
        where.accept(w);
        addWhere(w);
        return this;
    }

    public StandardSql order(Where where) {
        return this;
    }

    public StandardSql limit(int offset, int size) {
        return this;
    }


    public static class Where {
        private StandardSql sql;

        public Where(StandardSql sql) {
            this.sql = sql;
        }

        public Where add(String sql, Object... args) {
            return add(true, sql, args);
        }

        public Where add(boolean exp, String sql, Object... args) {
            return this;
        }

        public Where addCause(String sql) {
            return this;
        }

        public Where addArgs(Object... args) {
            return this;
        }
    }

    static Pattern pattern = Pattern.compile(":(\\w+)");

    public static List<NameParameter> findNameParameters(String sql) {
        List<NameParameter> parameters = new ArrayList<>();
        Matcher matcher = pattern.matcher(sql);
        int index = 1;
        while (matcher.find()) {
            parameters.add(new NameParameter(matcher.group(1), index++));
        }
        return parameters;
    }

    public static String normalizeSql(String sql) {
        if (sql.contains(":")) {
            return pattern.matcher(sql).replaceAll("?");
        }
        return sql;
    }

    public static void main(String[] args) {
        StandardSql sql = new StandardSql("select * from user");
        sql.where(w -> {
            w.add("username=?", "zero")
                    .add("123456".length() > 8, "and password=?", "123456");
            w.add("and age in ?", new Object[]{1, 2}, 1);
        });

        sql.where(w -> {
            w.add("username=#{username}");
            w.add("and age in ?", new Object[]{1, 2}, 1);
        });
    }

}
