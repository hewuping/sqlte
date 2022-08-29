package hwp.sqlte;

import hwp.sqlte.example.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

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


    public SqlBuilder select(String columns) {
        Objects.requireNonNull(columns);
        this.sql.append("SELECT ").append(columns).append(separator);
        return this;
    }

    public SqlBuilder select(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        this.sql.append("SELECT * FROM ").append(info.getTableName()).append(separator);
        return this;
    }

/*    public SqlBuilder select(String columns, String table) {
        return this.select(columns).from(table);
    }*/

    public SqlBuilder from(String from) {
        Objects.requireNonNull(from);
        if (sql.length() == 0) {
            this.sql.append("SELECT *").append(separator);
        }
        this.sql.append("FROM ").append(from).append(separator);
        return this;
    }

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
            sql.append(column + "=?");
            addArgs(values[i]);
        }
        sql.append(separator);
        return this;
    }

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

    public SqlBuilder sql(CharSequence sql, Object... args) {
        Objects.requireNonNull(sql);
        sql(sql).addArgs(args);
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
                continue;
            }
            if (arg instanceof Collection) {
                this.args.addAll((Collection) arg);
            } else {
                this.args.add(arg);
            }
        }
    }


    public SqlBuilder where(Where where) {
        if (where != null && !where.isEmpty()) {
            this.sql.append("WHERE ").append(where).append(separator);
            this.args.addAll(where.args());
        }
        return this;
    }

    public SqlBuilder where(Consumer<Where> consumer) {
        if (consumer == null) {
            return this;
        }
        Where where = new Where();
        consumer.accept(where);
        return this.where(where);
    }

    /**
     * 更具给定的 Example 对象生成查询条件
     *
     * @param example
     * @return
     * @since 0.2.13
     */
    public SqlBuilder where(Object example) {
        Class<?> clazz = example.getClass();
        Field[] fields = clazz.getFields();
        Where where = new Where();
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        try {
            for (Field field : fields) {
                Object value = Helper.getSerializedValue(example, field);
                if (value != null) {
                    if (value instanceof String && ((String) value).trim().isEmpty()) {
                        continue;
                    }
                    String column = info.getColumn(field);
                    if (Range.class.isInstance(value)) {
                        Range<?> range = (Range<?>) value;
                        Objects.requireNonNull(range.getStart(), field.getName() + ".start 不能为NULL");
                        Objects.requireNonNull(range.getEnd(), field.getName() + ".end 不能为NULL");
                        where.and(Condition.between(column, range.getStart(), range.getEnd()));
                        continue;
                    }
                    if (value.getClass().isArray()) {
                        where.and(Condition.in(column, value));
                        continue;
                    }
                    if (field.getAnnotation(StartWith.class) != null) {
                        where.and(Condition.startWith(column, value.toString()));
                        continue;
                    }
                    if (field.getAnnotation(EndWith.class) != null) {
                        where.and(Condition.endWith(column, value.toString()));
                        continue;
                    }
                    if (field.getAnnotation(Like.class) != null) {
                        where.and(Condition.like(column, value.toString()));
                        continue;
                    }
                    if (field.getAnnotation(Gte.class) != null) {
                        where.and(Condition.gte(column, value));
                        continue;
                    }
                    if (field.getAnnotation(Lte.class) != null) {
                        where.and(Condition.lte(column, value));
                        continue;
                    }
                    where.and(column + " = ?", value);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("构建 Where 错误", e);
        }
        return this.where(where);
    }

    public SqlBuilder where(Map<String, Object> andMap) {
        Where where = new Where();
        andMap.forEach((column, value) -> {
            if (column != null && value != null) {
                where.and(column + " =?", value);
            }
        });
        return this.where(where);
    }

    public SqlBuilder paging(int page, int size) {
        return this.limit(Math.max(0, (page - 1) * size), Math.max(size, 1));
    }

    public SqlBuilder limit(int first, int size) {
        this.sql.append("LIMIT ").append(first).append(", ").append(size).append(separator);
        return this;
    }

    public SqlBuilder limit(int size) {
        this.sql.append("LIMIT ").append(size).append(separator);
        return this;
    }

    public SqlBuilder orderBy(String orderSql) {
        Objects.requireNonNull(orderSql);
        sql.append("ORDER BY ").append(orderSql).append(separator);
        return this;
    }

    public SqlBuilder orderBy(Order order) {
        if (order != null && !order.isEmpty()) {
            sql.append("ORDER BY ").append(order).append(separator);
        }
        return this;
    }

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


    private void addSeparator() {
        if (sql.length() > 0 && Character.isSpaceChar(sql.charAt(sql.length() - 1))) {
            sql.append(separator);
        }
    }

}
