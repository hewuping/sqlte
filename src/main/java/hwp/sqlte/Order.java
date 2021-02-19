package hwp.sqlte;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zero
 * Created on 2017/3/22.
 */
public class Order {
    private List<String> columns;
    private List<Boolean> descs;

    public Order() {
    }

    public static Order by() {
        return new Order();
    }

    public static Order from(String[] columns, boolean[] descs) {
        return new Order().by(columns, descs);
    }

    public Order by(String[] columns, boolean[] descs) {
        if (columns.length != descs.length) {
            throw new IllegalArgumentException("columns的长度和descs的长度不一致");
        }
        this.columns = new ArrayList<>(columns.length);
        this.descs = new ArrayList<>(descs.length);
        return this;
    }

    public Order by(String column, boolean desc) {
        if (columns == null) {
            columns = new ArrayList<>(4);
            descs = new ArrayList<>(4);
        }
        columns.add(column);
        descs.add(desc);
        return this;
    }

    public Order asc(String column) {
        return this.by(column, false);
    }

    public Order desc(String column) {
        return this.by(column, true);
    }

    public String sql() {
        if (columns == null) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (sql.length() > 0) {
                sql.append(", ");
            }
            sql.append(columns.get(i));
            if (descs.get(i)) {
                sql.append(" DESC");
            }
        }
        return sql.toString();
    }

    public boolean isEmpty() {
        return columns == null || columns.isEmpty();
    }

    @Override
    public String toString() {
        return sql();
    }

}
