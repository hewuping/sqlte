package hwp.sqlte;

import hwp.sqlte.example.*;
import hwp.sqlte.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Where {

    private final StringBuilder whereBuilder = new StringBuilder();
    private final List<Object> whereArgs = new ArrayList<>(4);

    // 目前, allowEmpty 当为 false 时, 空条件在删除和更新操作中被禁止, 在查询中会输出警告日志
    private boolean allowEmpty;

    public static final Consumer<Where> EMPTY = where -> {
        where.allowEmpty = true;
    };

    public Where() {
        this.allowEmpty = false;
    }

    public Where(Consumer<Where> consumer) {
        consumer.accept(this);
    }

    /**
     * 允许查询条件为空(不包含任何条件)
     *
     * @return
     * @since 0.2.28
     */
    public Where allowEmpty() {
        allowEmpty = true;
        return this;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    /**
     * 拼接 SQL
     *
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return 该对象的引用
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
     * @return 该对象的引用
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
     * @return 该对象的引用
     */
    public <T> Where andIf(String sql, T arg, Predicate<T> test) {
        return and(test.test(arg), sql, arg);
    }

    /**
     * 使用 AND 操作符拼接 SQL
     *
     * @param sql SQL 片段, 有且仅有一个占位符
     * @param arg 如果参数值为  null 或 空字符串, 该 SQL 片段会被丢弃
     * @return 该对象的引用
     */
    public Where andIf(String sql, Object arg) {
        return this.and(ObjectUtils.isNotEmpty(arg), sql, arg);
    }

    /**
     * 使用 OR 操作符拼接 SQL
     *
     * @param sql  SQL 片段, 任意占位符数量
     * @param args 参数值, 数量与占位符保持一致
     * @return 该对象的引用
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
     * @return 该对象的引用
     */
    public <T> Where orIf(String sql, T arg, Predicate<T> test) {
        return or(test.test(arg), sql, arg);
    }

    /**
     * 使用 OR 操作符拼接 SQL
     *
     * @param sql SQL 片段, 有且仅有一个占位符
     * @param arg 如果参数值为  null 或 空字符串, 该 SQL 片段 会被丢弃
     * @return 该对象的引用
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
     * @return 该对象的引用
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
     * @return 该对象的引用
     */
    public Where or(boolean when, String sql, Object... args) {
        return append("OR", when, sql, args);
    }


    public Where exists(String sql, Object... args) {
        return append("AND", true, "EXISTS (" + sql + ")", args);
    }

    public Where exists(Consumer<SqlBuilder> consumer) {
        SqlBuilder sub = new SqlBuilder();
        consumer.accept(sub);
        return exists(sub.sql(), sub.args());
    }


    /**
     * 追加 SQL 片段
     *
     * @param operator 操作符, 比如: AND, OR 等, 当前操作符前面没有语句时, 该操作符会被忽略
     * @param when     当为 true 时, 后面的 sql 和 参数才会添加到 Builder 中
     * @param sql
     * @param args
     * @return 该对象的引用
     */
    public Where append(String operator, boolean when, String sql, Object... args) {
        if (when) {
            if (whereBuilder.length() > 0) {
                whereBuilder.append(" ");
                whereBuilder.append(operator);
                whereBuilder.append(" ");
            }
            String _sql = sql.toUpperCase();
            if (_sql.contains(" OR ") || _sql.contains(" AND ")) {
                if (_sql.startsWith("(") && _sql.endsWith(")")) {
                    whereBuilder.append(sql);
                } else {
                    whereBuilder.append('(').append(sql).append(')');
                }
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
     * <p> 通过 AND 操作符和 多个 {@link Condition } 生成构建 SQL
     * <p> 生成的 SQL类似: {@code AND (xxx AND xxx AND xxx) }
     *
     * @param conditions
     * @return 该对象的引用
     */
    public Where and(Condition... conditions) {
        if (conditions.length == 0) {
            return this;
        }
        return conditions("AND", "AND", conditions);
    }

    /**
     * <p> 通过 AND 操作符和单个 {@link Condition } 生成构建 SQL
     * <p> 当条件的参数值为空(包括: null, "", [])时, 会忽略该条件
     *
     * <pre>{@code
     *   where.andIf(Condition.eq("column_name", param_value));
     *   // 等同于
     *   if (param_value != null) {
     *     where.and(Condition.eq("column_name", param_value));
     *   }
     * } </pre>
     *
     * @param condition
     * @return 该对象的引用
     * @since 0.2.28
     */
    public Where andIf(Condition condition) {
        if (ObjectUtils.isEmpty(condition.args())) {
            return this;
        }
        return and(condition);
    }

    /**
     * 生成SQL: AND (key1=value1 AND key2=value2 AND key3=value3)
     *
     * @param map 通过 AND 连接的多个查询条件
     * @return 该对象的引用
     */
    public Where and(Map<String, ?> map) {
        map.forEach((key, value) -> {
            and(key + "=?", value);
        });
        return this;
    }

    /**
     * <p> 生成SQL: AND (key1=value1 AND key2=value2 AND key3=value3)
     * <p> 如果某个条件的值为空(包括: null, "", []), 则忽略该条件
     *
     * @param map 通过 AND 连接的多个查询条件
     * @return 该对象的引用
     * @since 0.2.28
     */
    public Where andIf(Map<String, ?> map) {
        map.forEach((key, value) -> {
            if (ObjectUtils.isNotEmpty(value)) {
                and(key + "=?", value);
            }
        });
        return this;
    }

    /**
     * OR (xxx OR xxx OR xxx)
     *
     * @param conditions 通过 OR 连接的多个条件
     * @return 该对象的引用
     */
    public Where or(Condition... conditions) {
        return conditions("OR", "OR", conditions);
    }

    /**
     * 通过 AND 和 OR 操作符构建 SQL : {@code AND (xxx OR xxx OR xxx) }
     *
     * @param conditions 通过 OR 连接的多个条件
     * @return 该对象的引用
     */
    public Where andOr(Condition... conditions) {
        return conditions("AND", "OR", conditions);
    }

    /**
     * 通过 OR 和  AND 操作符构建 SQL: {@code OR (xxx AND xxx AND xxx) }
     *
     * @param conditions 通过 AND 连接的多个条件
     * @return 该对象的引用
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
     * 根据对象构建条件
     *
     * @param example 查询条件
     * @return 该对象的引用
     */
    public Where of(Object example) {
        apply(Where.ofExample(example));
        return this;
    }

    /**
     * 返回带占位符的 SQL
     *
     * @return SQL
     */
    public String sql() {
        return whereBuilder.toString();
    }

    /**
     * 根据对象构造查询条件
     *
     * @param example
     * @return Where 对象
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
                String column = info.getColumn(field.getName());
//                if (Range.class.isInstance(value)) {
                if (value instanceof IRange) {
                    where.and(Condition.between(column, (IRange<?>) value));
                    continue;
                }
                if (value.getClass().isArray() || value instanceof Iterable || value instanceof Stream) {
                    where.and(Condition.in(column, value));
                    continue;
                }
                StartWith startWith = field.getAnnotation(StartWith.class);
                if (startWith != null) {
                    where.and(Condition.startWith(def(startWith.value(), column), value.toString()));
                    continue;
                }
                EndWith endWith = field.getAnnotation(EndWith.class);
                if (endWith != null) {
                    where.and(Condition.endWith(def(endWith.value(), column), value.toString()));
                    continue;
                }

                Like like = field.getAnnotation(Like.class);
                if (like != null) {
                    String val = value.toString();
                    String[] columns = like.columns();
                    if (columns.length == 0) {
                        columns = new String[]{column};
                    }
                    Condition[] ls = new Condition[columns.length];
                    for (int i = 0; i < columns.length; i++) {
                        ls[i] = Condition.contains(columns[i], val);
                    }
                    where.andOr(ls);
                    continue;
                }
                Gte gte = field.getAnnotation(Gte.class);
                if (gte != null) {
                    where.and(Condition.gte(def(gte.value(), column), value));
                    continue;
                }
                Lte lte = field.getAnnotation(Lte.class);
                if (lte != null) {
                    where.and(Condition.lte(def(lte.value(), column), value));
                    continue;
                }
                Gt gt = field.getAnnotation(Gt.class);
                if (gt != null) {
                    where.and(Condition.gt(def(gt.value(), column), value));
                    continue;
                }
                Lt lt = field.getAnnotation(Lt.class);
                if (lt != null) {
                    where.and(Condition.lt(def(lt.value(), column), value));
                    continue;
                }
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }
//                if ("page".equals(column) || "pageSize".equals(column) || "sort".equals(column)) {
//                    // 忽略排序分页字段
//                    continue;
//                }
                where.and(column + " = ?", value);
            }
        }
        return where;
    }

    /**
     * 使用已存在的 Map 创建新的 Where 对象
     *
     * @param map 查询条件
     * @return 该对象的引用
     */
    public static Where ofMap(Map<String, Object> map) {
        Where where = new Where();
        map.forEach((col, val) -> {
            if (col != null && val != null) {
                where.and(Condition.eq(col, val));
            }
        });
        return where;
    }

    /**
     * 通过构建 Map 方式创建新的 Where 对象
     *
     * @param map 查询条件
     * @return 该对象的引用
     */
    public static Where ofMap(Consumer<Map<String, Object>> consumer) {
        Map<String, Object> map = new LinkedHashMap<>();
        consumer.accept(map);
        return ofMap(map);
    }

    private static String def(String val, String def) {
        if (val == null || val.isEmpty()) {
            return def;
        }
        return val;
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

    protected void check() {
        if (!allowEmpty && isEmpty()) {
            throw new WhereException("WHERE clause cannot be empty. You can change this using allowEmpty()");
        }
    }

}
