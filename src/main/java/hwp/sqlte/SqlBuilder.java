package hwp.sqlte;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class SqlBuilder implements Builder, Sql {
    private char separator = ' ';

    private final List<Object> args = new ArrayList<>();
    private final StringBuilder sql = new StringBuilder();

    public SqlBuilder() {
    }

    public SqlBuilder(char separator) {
        this.separator = separator;
    }

    @Override
    public String sql() {
        int last = sql.length() - 1;
        if (Character.isSpaceChar(sql.charAt(last))) {
            sql.deleteCharAt(last);
        }
        return sql.toString();
    }

    @Override
    public Object[] args() {
        return this.args.toArray();
    }

    /**
     * 用于构建 CTE（Common Table Expressions，公共表达式）
     * <p>
     * CTE 例子:
     * <pre>{@code
     * WITH filtered_orders AS (
     *   SELECT customer_id, COUNT(*) AS order_count
     *   FROM orders
     *   WHERE order_amount > 1000
     *   GROUP BY customer_id
     * )
     * SELECT customers.name, filtered_orders.order_count
     * FROM filtered_orders
     * JOIN customers ON customers.id = filtered_orders.customer_id;
     * }</pre>
     *
     * @param name
     * @param sqlBuilder
     * @return
     * @since 0.2.24
     */
    public SqlBuilder withAs(String name, Consumer<SqlBuilder> consumer) {
        sql.append("WITH ").append(name).append(" AS (\n    ");
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        append(sub);
        sql.append("\n)\n");
        return this;
    }


    /**
     * 用于构建 CTE（Common Table Expressions，公共表达式）
     * <p>
     * CTE 例子:
     * <pre>{@code
     *    conn.with(cte -> {
     *         cte.set("xxx", sql -> {
     *
     *         });
     *         cte.set("yyy", sql -> {
     *
     *         });
     *     });
     * }</pre>
     *
     * @param consumer
     * @return
     * @since 0.2.24
     */
    public SqlBuilder with(Consumer<CTE> consumer) {
        sql.append("WITH ");
        CTE cte = new CTE();
        consumer.accept(cte);
        AtomicBoolean first = new AtomicBoolean(true);
        cte.forEach((name, sb) -> {
            if (!first.get()) {
                sql.append(",\n");
            }
            sql.append(name).append(" AS (\n");
            append(sb);
            sql.append("\n)");
            first.set(false);
        });
        sql.append("\n");
        return this;
    }


    /**
     * 例子: select("column1,column2")
     * <p>
     * 生成 SQL: SELECT column1,column2
     *
     * @param columns 列名, 使用英文逗号分隔
     * @return
     */
    public SqlBuilder select(String columns) {
        Objects.requireNonNull(columns);
        this.sql.append("SELECT ").append(columns).append(separator);
        return this;
    }

    /**
     * 生成 SQL: SELECT * FROM <i>table_name</i>
     *
     * @param clazz 用于获取表名
     * @return
     */
    public SqlBuilder select(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        this.sql.append("SELECT * FROM ").append(info.getTableName()).append(separator);
        return this;
    }

/*    public SqlBuilder select(String columns, String table) {
        return this.select(columns).from(table);
    }*/

    /**
     * 如果在调用 from() 前没有构建任何 SQL则生成: {@code SELECT * FROM table_name} (在 FROM 前插入 SELECT *),
     * 否则直接作为 sql 片段追加到 FROM 关键字后面
     *
     * @param table 表名 或 SQL
     * @return
     */
    public SqlBuilder from(String table) {
        Objects.requireNonNull(table);
        if (sql.length() == 0) {
            this.sql.append("SELECT *").append(separator);
        }
        this.sql.append("FROM ").append(table).append(separator);
        return this;
    }

    /**
     * SELECT COUNT(*)
     *
     * @param from
     * @return
     */
    public SqlBuilder selectCount(String from) {
        Objects.requireNonNull(from);
        sql.append("SELECT COUNT(*) AS _COUNT_ FROM ");
        if (from.indexOf(' ') != -1) {
            sql.append('(').append(from).append(") AS __TABLE__");
        } else {
            sql.append(from);
        }
        sql.append(separator);
        return this;
    }

    /**
     * 生成更新 SQL
     *
     * @param table   表名
     * @param columns 列名, 多列使用英文逗号分隔
     * @param values  新值, 值顺序更列名保持顺序一致
     * @return
     */
    public SqlBuilder update(String table, String columns, Object... values) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(columns);
        sql.append("UPDATE ").append(table);
        String[] split = columns.split(",");
        sql.append(" SET ");
        for (int i = 0; i < split.length; i++) {
            String column = split[i];
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(column.trim() + "=?");
            addArgs(values[i]);
        }
        sql.append(separator);
        return this;
    }

    /**
     * 生成删除 SQL:  DELETE FROM <i>table_name</i>
     *
     * @param table 表名
     * @return
     */
    public SqlBuilder delete(String table) {
        sql.append("DELETE FROM ").append(table).append(separator);
        return this;
    }

    public SqlBuilder sql(CharSequence sql) {
        Objects.requireNonNull(sql);
        this.sql.append(sql);
        char lastChar = sql.charAt(sql.length() - 1);
        if (!Character.isSpaceChar(lastChar)) {
            this.sql.append(separator);
        }
        return this;
    }

    /**
     * 追加子 SQL
     *
     * @param consumer
     * @return
     * @since 0.2.24
     */
    public SqlBuilder sql(Consumer<SqlBuilder> consumer) {
        Objects.requireNonNull(consumer);
        SqlBuilder sb = new SqlBuilder();
        consumer.accept(sb);
        return append(sb);
    }

    /**
     * 追加 SQL 片段, 确保占位符数量和值数量保持一致
     *
     * @param sql  SQL 片段
     * @param args SQL 片段中占位符对应的值
     * @return
     */
    public SqlBuilder sql(CharSequence sql, Object... args) {
        Objects.requireNonNull(sql);
        sql(sql).addArgs(args);
        return this;
    }

    /**
     * 使用新的参数值替换原来的值
     *
     * @param args
     * @return
     */
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
                continue;
            }
            if (arg instanceof Iterable) {
                Iterable<?> it = (Iterable<?>) arg;
                for (Object item : it) {
                    this.args.add(item);
                }
                continue;
            }
            this.args.add(arg);
        }
    }


    public SqlBuilder where(Where where) {
        if (where != null && !where.isEmpty()) {
            this.sql.append("WHERE ").append(where).append(separator);
            this.args.addAll(where.args());
        }
        return this;
    }

    /**
     * <pre>{@code
     *  sql.from("table_name").where(where -> {
     *      where.and(" column_name=?", value);
     *   });
     * } </pre>
     *
     * @param consumer
     * @return
     */
    public SqlBuilder where(Consumer<Where> consumer) {
        if (consumer == null) {
            return this;
        }
        Where where = new Where();
        consumer.accept(where);
        return this.where(where);
    }

    /**
     * 根据给定的 Example 对象生成查询条件
     *
     * @param example
     * @return
     * @since 0.2.13
     */
    public SqlBuilder where(Object example) {
        return this.where(Where.ofExample(example));
    }

    /**
     * 根据 Map 生成查询添加, 使用 AND 操作符连接
     * <pre> {@code
     * conn.where(Map.of("foo", "bar"))
     * } </pre>
     *
     * @param andMap
     * @return
     */
    public SqlBuilder where(Map<String, Object> andMap) {
        Where where = new Where();
        andMap.forEach((column, value) -> {
            if (column != null && value != null) {
                where.and(column + " =?", value);
            }
        });
        return this.where(where);
    }

    /**
     * 例子1 :
     * <pre>
     *     {@code sql.where("id=?", 1000) }
     * </pre>
     * <p>
     * 例子2 :
     * <pre>
     *     {@code sql.where("column1=? AND column2=?", "foo", "bar") }
     * </pre>
     *
     * @param sql
     * @param args
     * @return
     */
    public SqlBuilder where(String sql, Object... args) {
//      return this.where(where -> where.and(sql, args));
        return this.sql("WHERE " + sql, args);
    }

    /**
     * 分页
     *
     * @param page
     * @param size
     * @return
     */
    public SqlBuilder paging(int page, int size) {
        return this.limit(Math.max(0, (page - 1) * size), Math.max(size, 1));
    }

    public SqlBuilder paging(Pageable pageable) {
        return paging(pageable.getPage(), pageable.getPageSize());
    }

    public SqlBuilder limit(int first, int size) {
        this.sql.append("LIMIT ").append(first).append(", ").append(size).append(separator);
        return this;
    }

    public SqlBuilder limit(int size) {
        this.sql.append("LIMIT ").append(size).append(separator);
        return this;
    }

    /**
     * 排序, 例子:
     * <pre>
     *     {@code orderBy("amount DESC") }
     * </pre>
     *
     * @param orderSql
     * @return
     */
    public SqlBuilder orderBy(String orderSql) {
        Objects.requireNonNull(orderSql);
        sql.append("ORDER BY ").append(orderSql).append(separator);
        return this;
    }

    /**
     * 排序, 例子:
     * <pre>
     *     {@code orderBy(Order.of("amount", Direction.DESC)) }
     * </pre>
     *
     * @param order
     * @return
     */
    public SqlBuilder orderBy(Order order) {
        if (order != null && !order.isEmpty()) {
            sql.append("ORDER BY ").append(order).append(separator);
        }
        return this;
    }

    /**
     * <pre>{@code
     *  sql.orderBy(order -> {
     *     order.asc("username");
     *     order.desc("age");
     *   });
     * } </pre>
     *
     * @param consumer
     * @return
     */
    public SqlBuilder orderBy(Consumer<Order> consumer) {
        Order order = new Order();
        consumer.accept(order);
        return orderBy(order);
    }


    public SqlBuilder groupBy(String groupSql) {
        Objects.requireNonNull(groupSql);
        sql.append("GROUP BY ").append(groupSql).append(separator);
        return this;
    }

    public SqlBuilder groupBy(String groupSql, Consumer<Having> having) {
        Objects.requireNonNull(groupSql);
        sql.append("GROUP BY ").append(groupSql).append(separator);
        if (having != null) {
            Having h = new Having();
            having.accept(h);
            having(h);
        }
        return this;
    }

    private SqlBuilder having(Having having) {
        Objects.requireNonNull(having);
        if (!having.isEmpty()) {
            this.sql.append("HAVING ").append(having).append(separator);
            this.args.addAll(having.args());
        }
        return this;
    }

    public SqlBuilder append(String sql) {
        Objects.requireNonNull(sql);
        this.sql.append(sql).append(separator);
        return this;
    }

    public SqlBuilder append(String sql, Object... args) {
        Objects.requireNonNull(sql);
        this.sql.append(sql).append(separator);
        this.addArgs(args);
        return this;
    }

    private SqlBuilder appendSeparator() {
        if (sql.length() > 0 && !isEndWithSeparator(sql)) {
            sql.append(separator);
        }
        return this;
    }

    private boolean isEndWithSeparator(CharSequence str) {
        char c = str.charAt(str.length() - 1);
        return isSeparator(c);
    }

    private boolean isSeparator(char c) {
        return c == separator || c == ' ' || c == '\n';
    }

    public SqlBuilder append(SqlBuilder sub) {
        Objects.requireNonNull(sub);
        this.append(sub.sql(), sub.args());
        return this;
    }

    /**
     * 结果集合并成一个新的结果集，并且去除重复的行
     *
     * @param consumer
     * @return
     */
    public SqlBuilder union(Consumer<SqlBuilder> consumer) {
        append("\nUNION\n");
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        append(sub);
        return this;
    }

    /**
     * 结果集合并成一个新的结果集，不去除重复的行
     *
     * @param consumer
     * @return
     */
    public SqlBuilder unionAll(Consumer<SqlBuilder> consumer) {
        append("\nUNION ALL\n");
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        append(sub);
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
