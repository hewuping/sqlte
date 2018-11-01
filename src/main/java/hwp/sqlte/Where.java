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
                whereBuilder.append(" WHERE");
            }
            if (whereBuilder.length() > 8) {
                whereBuilder.append(" ");
                whereBuilder.append(operator);
                whereBuilder.append(" ");
            }
            whereBuilder.append(" ");
            whereBuilder.append(sql);
            Collections.addAll(whereArgs, args);
        }
        return this;
    }


    public void apply(Where where) {
        this.whereBuilder = where.whereBuilder;
        this.whereArgs = where.whereArgs;
    }

    protected List<Object> args() {
        return whereArgs;
    }


    //apply(where)
    //limit()
    //orderBy
    //ascBy
    //
    @Override
    public String toString() {
        return whereBuilder.toString();
    }
/*

    for (int i = 0; i < args.length; i++) {
        Object obj = args[i];
        if (obj.getClass().isArray()) {
            //添加占位: ?3
            int index = -1;
            for (int x = 0; x < whereBuilder.length(); x++) {
                if ('?' == whereBuilder.charAt(x)) {
                    index++;
                    if (index == whereArgs.size()) {
                        whereBuilder.insert(x, Array.getLength(obj));
                    }
                }
            }
            //展开数组
            for (int x = 0, size = Array.getLength(obj); x < size; x++) {
                whereArgs.add(Array.get(obj, x));
            }
        } else {
            whereArgs.add(obj);
        }
    }
*/

}
