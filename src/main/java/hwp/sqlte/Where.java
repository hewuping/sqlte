package hwp.sqlte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Where {
    private StringBuilder whereBuilder = new StringBuilder();
    private List<Object> whereArgs = new ArrayList<>(4);


    public Where add(String sql) {
        whereBuilder.append(sql);
        return this;
    }

    public Where and(String sql, Object... args) {
        return and(true, sql, args);
    }

    public Where or(String sql, Object... args) {
        return or(true, sql, args);
    }

    public Where and(boolean filter, String sql, Object... args) {
        return add("AND", filter, sql, args);
    }

    public Where or(boolean filter, String sql, Object... args) {
        return add("OR", filter, sql, args);
    }

    public Where add(String operator, boolean filter, String sql, Object... args) {
        if (filter) {
            if (whereBuilder.length() == 0) {
                whereBuilder.append(" ").append(keyword());
            }
            if (whereBuilder.length() > 8) {
                whereBuilder.append(" ");
                whereBuilder.append(operator);
            }
            whereBuilder.append(" ");
            whereBuilder.append(sql);
            Collections.addAll(whereArgs, args);
        }
        return this;
    }


/*    public void apply(Where where) {
        this.whereBuilder = where.whereBuilder;
        this.whereArgs = where.whereArgs;
    }*/

    protected List<Object> args() {
        return whereArgs;
    }

    protected boolean isEmpty() {
        return whereBuilder.length() == 0;
    }

    protected String keyword() {
        return "WHERE";
    }

    public Where and(Condition... conditions) {
        if (conditions.length == 1) {
            return and(true, conditions[0].sql(), conditions[0].args());
        } else if (conditions.length > 1) {
            StringBuilder builder = new StringBuilder();
            List<Object> args = new ArrayList<>();
            builder.append('(');
            for (int i = 0; i < conditions.length; i++) {
                Condition condition = conditions[i];
                if (i > 0) {
                    builder.append(" AND ");
                }
                builder.append(condition.sql());
                for (Object arg : condition.args()) {
                    args.add(arg);
                }
            }
            builder.append(')');
            return and(true, builder.toString(), args.toArray());
        }
        return this;
    }

    public Where or(Condition... conditions) {
        if (conditions.length == 1) {
            return and(true, conditions[0].sql(), conditions[0].args());
        } else if (conditions.length > 1) {
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            List<Object> args = new ArrayList<>();
            for (int i = 0; i < conditions.length; i++) {
                Condition condition = conditions[i];
                if (i > 0) {
                    builder.append(" OR ");
                }
                builder.append(condition.sql());
                for (Object arg : condition.args()) {
                    args.add(arg);
                }
            }
            builder.append(')');
            return or(true, builder.toString(), args.toArray());
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
