package hwp.sqlte;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class SqlBuilder implements Builder, Sql {
    private List<Object> args = new ArrayList<>();
    private StringBuilder sql = new StringBuilder();

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


    public SqlBuilder select(String table) {
        this.sql.append("SELECT * FROM ").append(table);
        return this;
    }

    public SqlBuilder select(String table, String columns) {
        this.sql.append("SELECT ").append(columns).append(" FROM ").append(table);
        return this;
    }

    public SqlBuilder selectCount(String table) {
        this.sql.append("SELECT count(*) as _count FROM ").append(table);
        return this;
    }

    public SqlBuilder update(String table, String columns, Object... values) {
        add("UPDATE ").add(table);
        String[] split = columns.split(",");
        add(" SET ");
        for (int i = 0; i < split.length; i++) {
            String column = split[i];
            if (i > 0) {
                add(", ");
            }
            add(column + "=?", values[i]);
        }
        return this;
    }

    public SqlBuilder delete(String table) {
        return add("DELETE FROM ").add(table);
    }

    public SqlBuilder sql(CharSequence sql) {
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
            if (arg == null) {
                this.args.add(null);
                continue;
            }
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
        if (!where.isEmpty()) {
            this.sql.append(where);
            this.args.addAll(where.args());
        }
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

    public SqlBuilder orderBy(String orderSql) {
        sql.append(" ORDER BY ").append(orderSql);
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

    public SqlBuilder groupBy(Consumer<Group> group) {
        return groupBy(group, null);
    }

    public SqlBuilder groupBy(Consumer<Group> group, Consumer<Having> having) {
        Group g = new Group();
        group.accept(g);
        add(g.sql());
        if (having != null) {
            Having h = new Having();
            having.accept(h);
            having(h);
        }
        return this;
    }

    private SqlBuilder having(Where where) {
        if (!where.isEmpty()) {
            this.sql.append(where.sql());
            this.args.addAll(where.args());
        }
        return this;
    }

    public SqlBuilder add(String sql) {
        this.sql.append(sql);
        return this;
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


    @Override
    public Sql build() {
        return new SimpleSql(sql(), args());
    }

}
