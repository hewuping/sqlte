package hwp.sqlte;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Zero
 * Created on 2017/3/22.
 */
public class Order {

    private final Map<String, Direction> items = new LinkedHashMap<>();

    public Order() {
    }

    public static Order by() {
        return new Order();
    }

    public Order by(LinkedHashMap<String, Direction> sort) {
        items.putAll(sort);
        return this;
    }

    public Order by(String column, Direction direction) {
        items.put(column, direction);
        return this;
    }

    public Order by(String column, boolean desc) {
        items.put(column, desc ? Direction.DESC : Direction.ASC);
        return this;
    }

    public Order asc(String column) {
        return this.by(column, Direction.ASC);
    }

    public Order desc(String column) {
        return this.by(column, Direction.DESC);
    }

    public String sql() {
        if (items.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        items.entrySet().forEach(entry -> {
            if (sql.length() > 0) {
                sql.append(", ");
            }
            sql.append(entry.getKey()).append(" ").append(entry.getValue());
        });
        return sql.toString();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public String toString() {
        return sql();
    }

}
