package hwp.sqlte;

import hwp.sqlte.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class SqlBuilder implements Builder, Sql {

    private static final Logger logger = LoggerFactory.getLogger(SqlBuilder.class);
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
     * 用于构建 CTE（Common Table Expressions，公用表表达式）
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
        this.append("WITH ").append(name).append(" AS (\n    ");
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        append(sub);
        this.append("\n)\n");
        return this;
    }


    /**
     * 用于构建 CTE（Common Table Expressions，公用表表达式）
     * <p>
     * CTE 例子:
     * <ul>
     *     <li>每个客户的总订单数、总订单金额以及平均订单金额</li>
     *     <li>前五个平均订单金额最高的客户姓名和平均订单金额</li>
     * </ul>
     *
     * <pre>{@code
     *  WITH customer_orders AS (
     *   SELECT customer_id,
     *          COUNT(*) AS order_count,
     *          SUM(total_amount) AS total_amount,
     *          AVG(total_amount) AS avg_amount
     *   FROM orders
     *   GROUP BY customer_id
     * ), -- 多个使用 逗号 分割
     * ranked_customers AS (
     *   SELECT c.name, o.avg_amount,
     *          DENSE_RANK() OVER (ORDER BY o.avg_amount DESC) AS rank
     *   FROM customer_orders o
     *   JOIN customers c ON o.customer_id = c.id
     * ) -- 最后一个不要添加逗号
     * SELECT name, avg_amount
     * FROM ranked_customers
     * WHERE rank <= 5;
     * } </pre>
     * <p>
     * 下面写法同上:
     * <pre>{@code
     *    conn.with(cte -> {
     *         cte.set("customer_orders", sql -> {
     *
     *         });
     *         cte.set("ranked_customers", sql -> {
     *
     *         });
     *     });
     * }</pre>
     * 对于没有 where 的语句, 建议直接使用"文本块"可读性更高
     *
     * @param consumer
     * @return
     * @since 0.2.24
     */
    public SqlBuilder with(Consumer<CTE> consumer) {
        this.append("WITH ");
        CTE cte = new CTE();
        consumer.accept(cte);
        AtomicBoolean first = new AtomicBoolean(true);
        cte.forEach((name, sb) -> {
            if (!first.get()) {
                this.append(",\n");
            }
            this.append(name).append(" AS (\n");
            append(sb);
            this.append("\n)");
            first.set(false);
        });
        this.append("\n");
        return this;
    }

//    public SqlBuilder with(String sql) {
//        this.append("WITH ");
//        this.append(sql);
//        this.append("\n");
//        return this;
//    }


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
        this.append("SELECT ").append(columns);
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
        this.append("SELECT * FROM ").append(info.getTableName());
        return this;
    }

/*    public SqlBuilder select(String columns, String table) {
        return this.select(columns).from(table);
    }*/

    /**
     * <p> 构建 SQL {@code FROM table_name}
     * <p> 如果前序没有内容则生成 {@code SELECT * FROM table_name}
     *
     * <p> table 也可以是不带参数的子查询, 例如:
     * <pre>{@code
     * SqlBuilder sb = new SqlBuilder();
     * sb.from("(SELECT * FROM xxx WHERE xxx=yyy)"); // 注意需要前后使用括号包裹
     * }</pre>
     *
     * @param table 表名 或 SQL
     * @return
     */
    public SqlBuilder from(String table) {
        Objects.requireNonNull(table);
        if (sql.length() == 0) {
            this.append("SELECT *");
        }
        this.append("FROM ").append(table);
        return this;
    }

    /**
     * 从子查询中查询
     * <p>
     * 例如: {@code SELECT * FROM ( SELECT * FROM users WHERE tenant_id=? ) WHERE email=? }
     * 我们可以使用如下方法动态构建子查询:
     *
     * <pre>{@code
     * SqlBuilder sql = new SqlBuilder();
     * sql.select("*").from(sub -> {
     *     sub.from("users").where(where -> {
     *          where.and("tenant_id=?", 10000);
     *     });
     * });
     * sql.where(where -> {
     *     where.and("email=?", "zero@example.com");
     * });
     * } </pre>
     *
     * @param consumer
     * @return
     * @since 0.2.28
     */
    public SqlBuilder from(Consumer<SqlBuilder> consumer) {
        Objects.requireNonNull(consumer);
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        String subSql = sub.sql();
        if (StringUtils.isBlank(subSql)) {
            throw new IllegalArgumentException("Subquery cannot be empty");
        }
        if (!subSql.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("Invalid subquery: " + subSql);
        }
        this.append("FROM (").append(subSql).append(")", sub.args());
        return this;
    }

    /**
     * 生成 SELECT COUNT(*)
     * <p>
     * 例如 1: {@code SELECT COUNT(*) AS _COUNT_ FROM <table> }
     * <p>
     * 例如 2: {@code SELECT COUNT(*) AS _COUNT_ FROM ( <sub sql> ) AS __TABLE__}
     *
     * @param from
     * @return
     */
    public SqlBuilder selectCount(String from) {
        Objects.requireNonNull(from);
        this.append("SELECT COUNT(*) AS _COUNT_ FROM ");
        if (from.indexOf(' ') != -1) {
            this.append("(").append(from).append(") AS __TABLE__");
        } else {
            this.append(from);
        }
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
        this.append("UPDATE ").append(table);
        String[] split = columns.split(",");
        this.append("SET");
        for (int i = 0; i < split.length; i++) {
            String column = split[i];
            if (i > 0) {
                this.append(", ");
            }
            this.append(column.trim() + "=?");
            addArgs(values[i]);
        }
        return this;
    }

    /**
     * 生成更新 SQL, 例如: {@code UPDATE table_name SET key1=?, key2=?, ... }
     *
     * @param table    表名
     * @param consumer 更新的列名和对于的值
     * @return
     */
    public SqlBuilder update(String table, Consumer<Map<String, Object>> consumer) {
        Map<String, Object> data = new LinkedHashMap<>();
        consumer.accept(data);
        return update(table, data);
    }

    /**
     * 生成更新 SQL, 例如: {@code UPDATE table_name SET key1=?, key2=?, ... }
     *
     * @param table 表名
     * @param data  更新的列名和对于的值
     * @return
     */
    public SqlBuilder update(String table, Map<String, Object> data) {
        this.append("UPDATE ").append(table).append(" SET ");
        Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            this.append(entry.getKey()).append("=?", entry.getValue());
            if (it.hasNext()) {
                this.append(", ");
            }
        }
        return this;
    }

    /**
     * 生成删除 SQL:  DELETE FROM <i>table_name</i>
     *
     * @param table 表名
     * @return
     */
    public SqlBuilder delete(String table) {
        this.append("DELETE FROM ").append(table);
        return this;
    }

    /**
     * 追加原始 SQL 片段
     *
     * @param sql
     * @return
     */
    public SqlBuilder sql(CharSequence sql) {
        this.append(sql);
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
     * @since 0.2.24
     */
    public Object args(int index) {
        return args.get(index);
    }

    /**
     * 使用新的参数值替换原来的值
     *
     * @param args
     * @return
     * @deprecated 使用 setArgs() 替代
     */
    @Deprecated
    public SqlBuilder args(Object... args) {
        if (this.args.size() > 0) {
            this.args.clear();
        }
        addArgs(args);
        return this;
    }


    /**
     * 使用新的参数值替换原来的值
     *
     * @param args
     * @return
     * @since 0.2.24
     */
    public SqlBuilder setArgs(Object... args) {
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


    /**
     * 追加 Where 查询条件, 例如:
     *
     * <pre>{@code
     *  Where where = new Where();
     *  where.and("column_name=?", value);
     *  sql.from("table_name").where(where);
     * }</pre>
     *
     * @param where
     * @return
     */
    public SqlBuilder where(Where where) {
        if (where != null && !where.isEmpty()) {
            this.append("WHERE ").append(where.sql());
            this.args.addAll(where.args());
        }
        //TODO 这里后续可能会改为抛异常
        if (!where.isAllowEmpty() && where.isEmpty()) {
            // WHERE clause cannot be empty.
            logger.warn("Missing condition in where clause");
        }
        return this;
    }

    /**
     * 追加 Where 查询条件, 例如:
     * <pre>{@code
     *  sql.from("table_name").where(where -> {
     *      where.and("column_name=?", value);
     *  });
     * }</pre>
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
        // 这里需要判断是使用 LIMIT 还是 FETCH
        int offset = Math.max(0, (page - 1) * size);
        int _size = Math.max(size, 1);
        return this.limit(offset, _size);
    }

    /**
     * LIMIT 分页
     *
     * @param pageable
     * @return
     */
    public SqlBuilder paging(Pageable pageable) {
        return paging(pageable.getPage(), pageable.getPageSize());
    }

    /**
     * <p> 适用 MySQL: {@code LIMIT offset, size }, {@code LIMIT size OFFSET offset}
     * <p> 注意: SQL Server / Oracle 不适用
     *
     * @param offset
     * @param size
     * @return
     */
    public SqlBuilder limit(int offset, int size) {
        this.append("LIMIT ").append(size);
        if (offset > 0) {
            this.append(" OFFSET ").append(offset);
        }
        return this;
    }

    /**
     * LIMIT size
     *
     * @param size
     * @return
     */
    public SqlBuilder limit(int size) {
        this.append("LIMIT ").append(size);
        return this;
    }

    /**
     * 使用 LIMIT 分页
     *
     * @param pageable
     * @return
     * @since 0.2.28
     */
    public SqlBuilder limit(Pageable pageable) {
        return paging(pageable);
    }

    /**
     * <p> Microsoft SQL Server: 从 2012 版本开始支持 OFFSET 和 FETCH NEXT 用法
     * <p> Oracle: 从 12c 版本开始 OFFSET 和 FETCH NEXT 用法
     *
     * <p> 例子: {@code fetch(30, 15)} 生成的 SQL 如下:
     * <pre>{@code
     * OFFSET 30 ROWS
     * FETCH NEXT 15 ROWS ONLY
     * }</pre>
     *
     * @param offset
     * @param size
     * @return
     * @since 0.2.28
     */
    public SqlBuilder fetch(int offset, int size) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0 ");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be > 0 ");
        }
        if (offset == 0) {
            append("FETCH FIRST " + size + " ROWS ONLY");
        } else {
            append("OFFSET " + offset + " ROWS");
            append("FETCH NEXT " + size + " ROWS ONLY");
        }
        return this;
    }

    public SqlBuilder fetch(Pageable page) {
        int offset = Math.max(0, (page.getPage() - 1) * page.getPageSize());
        int _size = Math.max(page.getPageSize(), 1);
        return this.fetch(offset, _size);
    }

    /**
     * 排序, 例子:
     * <pre>
     *     {@code orderBy("amount DESC") }
     * </pre>
     *
     * @param orderSql
     * @return 该对象的引用
     */
    public SqlBuilder orderBy(String orderSql) {
        Objects.requireNonNull(orderSql);
        this.append("ORDER BY ").append(orderSql);
        return this;
    }

    /**
     * 排序, 例子:
     * <pre>
     *     {@code orderBy(Order.of("amount", Direction.DESC)) }
     * </pre>
     *
     * @param order
     * @return 该对象的引用
     */
    public SqlBuilder orderBy(Order order) {
        if (order != null && !order.isEmpty()) {
            this.append("ORDER BY ").append(order.sql());
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
     * @return 该对象的引用
     */
    public SqlBuilder orderBy(Consumer<Order> consumer) {
        Order order = new Order();
        consumer.accept(order);
        return orderBy(order);
    }


    /**
     * 通过 GROUP BY 拼接 SQL
     *
     * @param groupSql SQL 片段
     * @return 该对象的引用
     */
    public SqlBuilder groupBy(String groupSql) {
        Objects.requireNonNull(groupSql);
        this.append("GROUP BY ").append(groupSql);
        return this;
    }

    /**
     * 通过 GROUP BY 拼接 SQL
     *
     * @param groupSql SQL 片段
     * @param having
     * @return 该对象的引用
     */
    public SqlBuilder groupBy(String groupSql, Consumer<Having> having) {
        Objects.requireNonNull(groupSql);
        this.append("GROUP BY ").append(groupSql);
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
            this.append("HAVING ").append(having.sql());
            this.args.addAll(having.args());
        }
        return this;
    }

    /**
     * 直接拼接 SQL, 不会添加任何关键字
     *
     * @param sql SQL 片段
     * @return 该对象的引用
     */
    public SqlBuilder append(CharSequence sql) {
        // 仅该方法可使用 this.sql.append()
        Objects.requireNonNull(sql);
        if (startWithSeparator(sql) || endWithSeparator(this.sql)) {
            this.sql.append(sql);
            return this;
        }
        this.sql.append(separator);
        this.sql.append(sql);
        return this;
    }

    private SqlBuilder append(Number number) {
        this.append(String.valueOf(number));
        return this;
    }

    /**
     * 直接拼接 SQL, 不会添加任何关键字
     *
     * @param sql  SQL 片段
     * @param args SQL 片段中占位符对于的参数值
     * @return 该对象的引用
     */
    public SqlBuilder append(String sql, Object... args) {
        Objects.requireNonNull(sql);
        this.append(sql);
        this.addArgs(args);
        return this;
    }


    private boolean startWithSeparator(CharSequence str) {
        char c = str.charAt(0);
        return isSeparator(c);
    }

    private boolean endWithSeparator(CharSequence str) {
        if (sql.length() == 0) {
            return true;
        }
        char c = str.charAt(str.length() - 1);
        return isSeparator(c);
    }

    private boolean isSeparator(char c) {
        return c == separator || c == ' ' || c == '\n' || c == ',';
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
