package hwp.sqlte;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Zero
 * Created on 2018/12/4.
 */
public class Condition {
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
     * begin and end values are included.
     *
     * @param column
     * @param begin
     * @param end
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
        if ("".equals(begin) || "".equals(end)) {
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

    public static Condition between(String column, Range<?> range) {
        return between(column, range.getStart(), range.getEnd());
    }

    public static Condition like(String column, String value) {
        return new Condition(column, " LIKE ", value);
    }

    public static Condition notLike(String column, String value) {
        return new Condition(column, " NOT LIKE ", value);
    }

    public static Condition startWith(String column, String value) {
        return like(column, value + "%");
    }

    public static Condition endWith(String column, String value) {
        return like(column, "%" + value);
    }

    public static Condition contains(String column, String value) {
        return like(column, "%" + value + "%");
    }

    public static Condition in(String column, String[] values) {
//        Object[] args = new Object[values.length];
//        System.arraycopy(values, 0, args, 0, values.length);
        return _in(false, column, (Object[]) values);
    }

    public static Condition in(String column, Object... values) {
        return _in(false, column, values);
    }

    public static Condition notIn(String column, Object... values) {
        return _in(true, column, values);
    }

    private static <T> Condition _in(boolean notIn, String column, Object... values) {
        // 展开数组和集合
        List<Object> args = new ArrayList<>(values.length);
        for (Object value : values) {
            if (value.getClass().isArray()) {
                Object[] va = (Object[]) value;
                for (Object o : va) {
                    args.add(o);
                }
            } else if (value instanceof Collection) {
                Collection<?> c = (Collection<?>) value;
                args.addAll(c);
            } else {
                args.add(value);
            }
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
