/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package hwp.sqlte;

public class Group {

    private StringBuilder groupSql = new StringBuilder();

    public Group by(boolean condition, String column) {
        if (condition) {
            if (groupSql.length() == 0) {
                groupSql.append(" GROUP BY");
            }
            groupSql.append(' ').append(column);
        }
        return this;
    }

    public Group by(String column) {
        return by(true, column);
    }

    public String sql() {
        return groupSql.toString();
    }

    @Override
    public String toString() {
        return sql();
    }

}
