package hwp.sqlte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Where {

    private final StringBuilder whereBuilder = new StringBuilder();
    private final List<Object> whereArgs = new ArrayList<>(4);

    public Where append(String sql, Object... args) {
        whereBuilder.append(sql);
        Collections.addAll(whereArgs, args);
        return this;
    }

    public Where and(String sql, Object... args) {
        return and(true, sql, args);
    }

    public Where or(String sql, Object... args) {
        return or(true, sql, args);
    }

    public Where and(boolean when, String sql, Object... args) {
        return append("AND", when, sql, args);
    }

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


    public void apply(Where where) {
        // 避免修改参数
        this.whereBuilder.setLength(0);
        this.whereArgs.clear();
        this.whereBuilder.append(where.whereBuilder);
        this.whereArgs.addAll(where.args());
    }

    public List<Object> args() {
        return whereArgs;
    }

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

    //apply(where)
    //limit()
    //orderBy
    //ascBy
    //
    @Override
    public String toString() {
        return sql();
    }

    public String sql() {
        return whereBuilder.toString();
    }

}
