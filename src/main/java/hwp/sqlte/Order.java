package hwp.sqlte;

import hwp.sqlte.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
/*
    public static Order of(Sort sort) {
        return new Order().by(sort);
    }*/

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


    /**
     * @param sortStr  eg: field:desc,name:asc
     * @param consumer fieldName -> columnName
     * @return
     */
    public Order apply(String sortStr, Consumer<Map<String, String>> consumer) {
        Map<String, String> map = new LinkedHashMap<>();
        if (consumer != null) {
            consumer.accept(map);
        }
        if (sortStr == null || sortStr.isEmpty()) {
            return this;
        }
        List<String> fields = StringUtils.split(sortStr, ",", true);
        for (String field : fields) {
            List<String> ss = StringUtils.split(field, ":", true);
            if (ss.size() != 2) {
                continue;
            }
            String name = ss.get(0);
            Direction direction = Direction.find(ss.get(1));
            if (name == null || direction == null) {
                continue;
            }
            //避免修改URL导致SQL异常, 这里必须明确指定映射, 基本名称一样
            String columnName = map.get(name);
            if (columnName != null) {
                this.by(columnName, direction);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return sql();
    }

}
