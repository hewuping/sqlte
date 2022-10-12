package hwp.sqlte;

import hwp.sqlte.example.*;
import hwp.sqlte.util.ObjectUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Where {

    private final StringBuilder whereBuilder = new StringBuilder();
    private final List<Object> whereArgs = new ArrayList<>(4);

    public Where() {
    }

    public Where(Consumer<Where> consumer) {
        consumer.accept(this);
    }

    /**
     * 拼接 SQL
     *
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return
     */
    public Where append(String sql, Object... args) {
        whereBuilder.append(sql);
        Collections.addAll(whereArgs, args);
        return this;
    }

    /**
     * 使用 AND 操作符拼接 SQL
     *
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return
     */
    public Where and(String sql, Object... args) {
        return and(true, sql, args);
    }

    /**
     * 使用 AND 操作符拼接 SQL
     *
     * @param sql  SQL 片段, 任意占位符数量
     * @param arg  如果参数值为  null 或 空字符串, 该 SQL片段 会被丢弃
     * @param test 如果返回 false 则忽略 SQL 片段
     * @return
     */
    public <T> Where andIf(String sql, T arg, Predicate<T> test) {
        return and(test.test(arg), sql, arg);
    }

    /**
     * 使用 AND 操作符拼接 SQL
     *
     * @param sql SQL 片段, 有且仅有一个占位符
     * @param arg 如果参数值为  null 或 空字符串, 该 SQL 片段会被丢弃
     * @return
     */
    public Where andIf(String sql, Object arg) {
        return this.and(ObjectUtils.isNotEmpty(arg), sql, arg);
    }

    /**
     * 使用 OR 操作符拼接 SQL
     *
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return
     */
    public Where or(String sql, Object... args) {
        return or(true, sql, args);
    }

    /**
     * 使用 OR 操作符拼接 SQL
     *
     * @param sql  SQL 片段, 有且仅有一个占位符
     * @param arg  如果参数值为  null 或 空字符串, 该 SQL 片段会被丢弃
     * @param test 如果返回 false 则忽略 SQL 片段
     * @return
     */
    public <T> Where orIf(String sql, T arg, Predicate<T> test) {
        return or(test.test(arg), sql, arg);
    }

    /**
     * 使用 OR 操作符拼接 SQL
     *
     * @param sql SQL 片段, 有且仅有一个占位符
     * @param arg 如果参数值为  null 或 空字符串, 该 SQL 片段 会被丢弃
     * @return
     */
    public Where orIf(String sql, Object arg) {
        return or(ObjectUtils.isNotEmpty(arg), sql, arg);
    }

    /**
     * 使用 AND 操作符拼接 SQL
     *
     * @param when 当条件为 true 时才拼接 SQL, 否则丢弃该 SQL 片段
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return
     */
    public Where and(boolean when, String sql, Object... args) {
        return append("AND", when, sql, args);
    }

    /**
     * 使用 OR 操作符拼接 SQL
     *
     * @param when 当条件为 true 时才拼接 SQL, 否则丢弃该 SQL 片段
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return
     */
    public Where or(boolean when, String sql, Object... args) {
        return append("OR", when, sql, args);
    }

    public Where append(String operator, boolean when, String sql, Object... args) {
        if (when) {
            if (whereBuilder.length() > 0) {
                whereBuilder.append(" ");
                whereBuilder.append(operator);
                whereBuilder.append(" ");
            }
            String _sql = sql.toUpperCase();
            if (_sql.contains(" OR ") || _sql.contains(" AND ")) {
                whereBuilder.append('(').append(sql).append(')');
            } else {
                whereBuilder.append(sql);
            }
            Collections.addAll(whereArgs, args);
        }
        return this;
    }

    /**
     * 使用 指定的 where 对象代替本身
     *
     * @param where
     */
    public void apply(Where where) {
        // 避免修改参数
        this.whereBuilder.setLength(0);
        this.whereArgs.clear();
        this.whereBuilder.append(where.whereBuilder);
        this.whereArgs.addAll(where.args());
    }

    /**
     * 参数值列表
     *
     * @return
     */
    public List<Object> args() {
        return whereArgs;
    }

    /**
     * 获取参数值 (基于0)
     *
     * @param index
     * @return
     */
    public Object args(int index) {
        return whereArgs.get(index);
    }

    /**
     * SQL 是否为空
     *
     * @return
     */
    protected boolean isEmpty() {
        return whereBuilder.length() == 0;
    }


    /**
     * AND (xxx AND xxx AND xxx)
     *
     * @param conditions
     * @return
     */
    public Where and(Condition... conditions) {
        return conditions("AND", "AND", conditions);
    }

    /**
     * OR (xxx OR xxx OR xxx)
     *
     * @param conditions
     * @return
     */
    public Where or(Condition... conditions) {
        return conditions("OR", "OR", conditions);
    }

    /**
     * AND (xxx OR xxx OR xxx)
     *
     * @param conditions
     * @return
     */
    public Where andOr(Condition... conditions) {
        return conditions("AND", "OR", conditions);
    }

    /**
     * OR (xxx AND xxx AND xxx)
     *
     * @param conditions
     * @return
     */
    public Where orAnd(Condition... conditions) {
        return conditions("OR", "AND", conditions);
    }

    private Where conditions(String operator, String operator2, Condition... conditions) {
        if (conditions.length == 1) {
            this.append(operator, true, conditions[0].sql(), conditions[0].args());
        } else if (conditions.length > 1) {
            StringBuilder builder = new StringBuilder();
            List<Object> args = new ArrayList<>();
            for (int i = 0; i < conditions.length; i++) {
                Condition condition = conditions[i];
                if (i > 0) {
                    builder.append(' ');
                    builder.append(operator2);
                    builder.append(' ');
                }
                builder.append(condition.sql());
                for (Object arg : condition.args()) {
                    args.add(arg);
                }
            }
            this.append(operator, true, builder.toString(), args.toArray());
        }
        return this;
    }

    /**
     * 返回带占位符的 SQL
     *
     * @return
     */
    public String sql() {
        return whereBuilder.toString();
    }

    /**
     * 根据对象构造查询条件
     *
     * @param example
     * @return
     * @since 0.2.15
     */
    public static Where ofExample(Object example) {
        Where where = new Where();
        Class<?> clazz = example.getClass();
        ClassInfo info = ClassInfo.getClassInfo(clazz);
        Field[] fields = info.getFields();
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
        return where;
    }

    //apply(where)
    //limit()
    //orderBy
    //ascBy
    //
    @Override
    public String toString() {
        return sql();
    }

}
