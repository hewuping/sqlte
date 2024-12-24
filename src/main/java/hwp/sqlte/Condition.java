package hwp.sqlte;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Zero
 * Created on 2018/12/4.
 */
public class Condition {

//    public static final Condition EMPTY = new Condition("", new Object[0]);

    private String sql;
    private Object[] args;

    private Condition() {
    }

    private Condition(String sql, Object[] args) {
        this.sql = sql;
        this.args = args;
    }

    private Condition(String column, String operator, Object value) {
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("'column' cannot both be null or empty");
        }
        this.sql = column + operator + "?";
        this.args = new Object[]{value};
    }

    public static Condition isNull(String column) {
        return new Condition(column + " IS NULL", new Object[0]);
    }

    public static Condition isNotNull(String column) {
        return new Condition(column + " IS NOT NULL", new Object[0]);
    }

    public static Condition eq(String column, Object value) {
        return new Condition(column, " = ", value);
    }

    public static Condition neq(String column, Object value) {
        return new Condition(column, " <> ", value);
    }


    public static Condition lt(String column, Object value) {
        return new Condition(column, " < ", value);
    }

    public static Condition lte(String column, Object value) {
        return new Condition(column, " <= ", value);
    }

    public static Condition gt(String column, Object value) {
        return new Condition(column, " > ", value);
    }

    public static Condition gte(String column, Object value) {
        return new Condition(column, " >= ", value);
    }

    /**
     * 使用 {@code BETWEEN <begin> AND <end>} 条件查询。 查询区间：[begin, end]
     * <p>类似:
     * {@code
     * where.and("xxx BETWEEN ? AND ?", begin, end)
     * }
     *
     * @param column 列名
     * @param begin  开始值(包含), 如果为 null 或 空 则变为 {@code <= end }
     * @param end    结束值(包含), 如果为 null 或 空 则变为 {@code <= begin }
     * @return
     */
    public static Condition between(String column, Object begin, Object end) {
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("'column' cannot be null or empty");
        }
        if (begin == null && end == null) {//begin, end
            throw new IllegalArgumentException("'begin' and 'end' cannot both be null");
        }
        if (begin != null && end != null && begin.getClass() != end.getClass()) {
            throw new IllegalArgumentException("Type of 'begin' and 'end' are inconsistent");
        }
        if ("".equals(begin) && "".equals(end)) {
            throw new IllegalArgumentException("'begin' and 'end' cannot both be empty");
        }
        if (begin == null) {
            return new Condition(column + " <= ?", new Object[]{end});
        }
        if (end == null) {
            return new Condition(column + " >= ?", new Object[]{begin});
        }
        return new Condition(column + " BETWEEN ? AND ?", new Object[]{begin, end});
    }

    /**
     * 使用 {@code BETWEEN <start> AND <end>} 条件查询。 查询区间：[start, end]
     *
     * @param column 列名
     * @param range  如果 {@code range.start = null} 则变为 {@code column <= end};
     *               如果 {@code range.end = null}  则变为 {@code column <= begin}
     * @return
     */
    public static Condition between(String column, IRange<?> range) {
        return between(column, range.getStart(), range.getEnd());
    }

    /**
     * 基于通配符的 LIKE 搜索, 通配符(wildcards)使用说明:
     * <ul>
     *     <li> %  - 替换零个或多个字符, 例如: wild% </li>
     *     <li> _  - 替换单个字符, 例如: wildca_d </li>
     *     <li> [] - 表示括号内的任何单个字符, 例如: wildca[r]d (SQLite 不支持)</li>
     *     <li> [^] - 表示不在括号中的任何字符 , 例如: wildca[^abc]d (SQLite 不支持)</li>
     *     <li> [-] - 表示指定范围内的任何单个字符 , 例如: wildca[a-z]d (SQLite 不支持)</li>
     * </ul>
     * 如果希望搜索: %_[]^- 字符, 需要转义, 例如: \_ (MySQL 转义字符为: \)
     *
     * <ul>
     *     <li> MySQL 转义字符为: \ </li>
     *     <li> SQL Server 转义字符为: ' </li>
     *     <li> Sqlite 转义字符为: ' </li>
     * </ul>
     * <p>
     * 部分数据支持在SQL中生命转义字符, 例如: SELECT * FROM `project` WHERE `name` LIKE '%#\%' ESCAPE '#'
     *
     * @param column 列名
     * @param value  可使用通配符的字符串
     * @return
     */
    public static Condition like(String column, String value) {
        return new Condition(column, " LIKE ", value);
    }

    /**
     * 基于正则表达式的搜索 ( MySQL )
     *
     * @param column
     * @param regex
     * @return
     */
    public static Condition rlike(String column, String regex) {
        // PostgreSQL: WHERE column_name SIMILAR TO 'pattern';
        // SQLite: WHERE column_name REGEXP 'pattern';
        // Oracle: WHERE REGEXP_LIKE(column_name, 'pattern');
        return new Condition(column, " RLIKE ", regex);
    }

    /**
     * 使用 NOT LIKE 条件查询, 生成 SQL: NOT LIKE value
     *
     * @param column 列名
     * @param value  值
     * @return
     */
    public static Condition notLike(String column, String value) {
        return new Condition(column, " NOT LIKE ", value);
    }

    /**
     * 使用 LIKE 条件查询, 生成 SQL: LIKE value%
     *
     * @param column 列名
     * @param value  值, 自动添加后缀 '%'
     * @return
     */
    public static Condition startWith(String column, String value) {
        return like(column, value + "%");
    }

    /**
     * 使用 LIKE 条件查询, 生成 SQL: LIKE %value
     *
     * @param column 列名
     * @param value  值, 自动添加前缀 '%'
     * @return
     */
    public static Condition endWith(String column, String value) {
        return like(column, "%" + value);
    }

    /**
     * 使用 LIKE 条件查询, 生成 SQL: LIKE %value%
     *
     * @param column 列名
     * @param value  值, 前后会自动添加 '%'
     * @return
     */
    public static Condition contains(String column, String value) {
        // SQL Server 可以使用函数: CONTAINS(column, value)
        // MySQL InnoDB 不支持 CONTAINS 函数, 可以使用 INSTR
        // 无法(搜索\字符会有问题)
        return like(column, "%" + value + "%");
    }


    private static String escapeWildcard(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\') {
                continue;
            }
            if (c == '_') {
                builder.append("\\_");
                continue;
            }
            if (c == '%') {
                builder.append("\\%");
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * 使用 IN 条件查询
     *
     * @param column 列名
     * @param values 字符串数组。
     * @return
     * @throws IllegalArgumentException 如果参数值为空抛出异常
     */
    public static Condition in(String column, String[] values) throws IllegalArgumentException {
//        Object[] args = new Object[values.length];
//        System.arraycopy(values, 0, args, 0, values.length);
        return _in(false, column, (Object[]) values);
    }

    /**
     * 使用 IN 条件查询
     *
     * @param column 列名
     * @param values 可变数组，也可以是 Array 或 Collection。
     * @return
     * @throws IllegalArgumentException 如果参数值为空抛出异常
     */
    public static <T> Condition in(String column, T... values) throws IllegalArgumentException {
        return _in(false, column, values);
    }

    /**
     * 使用 IN 条件查询
     *
     * @param column 列名
     * @param values 参数值
     * @param <E>
     * @return
     * @throws IllegalArgumentException 如果参数值为空抛出异常
     */
    public static <E> Condition in(String column, Iterable<E> values) throws IllegalArgumentException {
        return _in(false, column, values);
    }

    /**
     * 使用 NOT IN 条件查询
     *
     * @param column 列名
     * @param values 参数值, 可以是 Array, Collection 或 Stream
     * @return
     * @throws IllegalArgumentException 如果参数值为空抛出异常
     */
    public static Condition notIn(String column, Object... values) throws IllegalArgumentException {
        return _in(true, column, values);
    }

    private static <T> Condition _in(boolean notIn, String column, T... values) throws IllegalArgumentException {
        // 展开数组和集合
        List<Object> args = new ArrayList<>(values.length);
        for (Object value : values) {
            if (value.getClass().isArray()) {
                // 注意: values 参数只传递 int[]{1 ,2, 3} 时会被被作为一个数组大小为 1 且值为数组的的数组
                // 而 Integer[]{1 ,2, 3} 则不会
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    args.add(Array.get(value, i));
                }
            } else if (value instanceof Iterable) {
                Iterable<?> it = (Iterable<?>) value;
                it.forEach(o -> args.add(o));
            } else if (value instanceof Stream) {
                Stream<?> c = (Stream<?>) value;
                c.forEach(args::add);
            } else {
                args.add(value);
            }
        }
        if (args.size() == 0) {
            throw new IllegalArgumentException("IN 参数值不能为空");
        }
        // 构建sql
        StringBuilder builder = new StringBuilder(column);
        if (notIn) {
            builder.append(" NOT IN (");
        } else {
            builder.append(" IN (");
        }
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append('?');
        }
        builder.append(")");
        return new Condition(builder.toString(), args.toArray());
    }

    public String sql() {
        return sql;
    }

    public Object[] args() {
        return args;
    }
}
