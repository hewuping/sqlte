package hwp.sqlte;

import hwp.sqlte.util.Sort;
import hwp.sqlte.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Zero
 * Created on 2017/3/22.
 */
public class Order {

    private final Map<String, Direction> items = new LinkedHashMap<>();

    public Order() {
    }

    public static Order of() {
        return new Order();
    }

    public static Order of(String column, Direction direction) {
        Order order = new Order();
        order.by(column, direction);
        return order;
    }

/*
    public static Order of(Sort sort) {
        return new Order().by(sort);
    }*/

    public Order by(LinkedHashMap<String, Direction> sort) {
        items.putAll(sort);
        return this;
    }

    /**
     * 指定列名和排序方式
     *
     * @param column    表列名
     * @param direction 如果为 null 将忽略
     * @return
     */
    public Order by(String column, Direction direction) {
        if (direction == null) {
            return this;
        }
        items.put(column, direction);
        return this;
    }

    /**
     * 指定列名和排序方式
     *
     * @param column 表列名
     * @param desc   是否按倒序排序, 否则为升序
     * @return
     */
    public Order by(String column, boolean desc) {
        items.put(column, desc ? Direction.DESC : Direction.ASC);
        return this;
    }

    /**
     * 根据名称匹配排序, 一般结合 Sort 类使用
     *
     * <pre>{@code
     *  sql.orderBy(order -> {
     * 	order.by("user_id", sort::match, Direction.ASC);// 字段匹配, 默认为升序
     * 	order.by("created_at", sort::match);
     * }); }</pre>
     *
     * @param column 表列名
     * @param match  匹配方法
     * @param def    默认值
     * @return 如果未匹配到则返回 ASC
     * @since 0.2.24
     */
    public Order by(String column, Function<String, Direction> match, Direction def) {
        Direction dir = match.apply(column);
        if (dir == null) {
            if (def == null) {
                return this;
            }
            return by(column, def);
        }
        return by(column, dir);
    }

    /**
     * 根据名称匹配排序, 一般结合 Sort 类使用
     *
     * <pre>{@code
     *  sql.orderBy(order -> {
     * 	order.by("user_id", sort::match);// 字段匹配, 默认为升序
     * 	order.by("created_at", sort::match);
     * });
     * }</pre>
     *
     * @param column 表列名
     * @param match  匹配方法
     * @return 如果未匹配到则返回 ASC
     * @since 0.2.24
     */
    public Order by(String column, Function<String, Direction> match) {
        return by(column, match.apply(column));
    }

    /**
     * 按指定列名升序排序
     *
     * @param column 表列名
     * @return
     */
    public Order asc(String column) {
        return by(column, Direction.ASC);
    }


    /**
     * 默认为升序, 如果设置了 direction 参数, 则以 direction 为准
     *
     * <pre>{@code
     *  sql.orderBy(order -> {
     * 	  order.asc("user_id", newDir);// 字段匹配, 默认为升序
     *  });
     *  }</pre>
     *
     * @param column    表列名
     * @param direction 排序
     * @return
     * @since 0.2.25
     */
    public Order asc(String column, Direction direction) {
        return by(column, direction == null ? Direction.ASC : direction);
    }

    /**
     * 按指定列名降序排序
     *
     * @param column 表列名
     * @return
     */
    public Order desc(String column) {
        return by(column, Direction.DESC);
    }

    /**
     * 默认为降序, 如果设置了 direction 参数, 则以 direction 为准
     *
     * <pre>{@code
     *  sql.orderBy(order -> {
     * 	  order.desc("user_id", newDir);// 字段匹配, 默认为降序
     *  });
     *  }</pre>
     *
     * @param column    表列名
     * @param direction 排序
     * @return
     * @since 0.2.25
     */
    public Order desc(String column, Direction direction) {
        return by(column, direction == null ? Direction.DESC : direction);
    }

    /**
     * 对象模型转为 SQL 片段
     *
     * @return
     */
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

    /**
     * 排序规则是否为空
     *
     * @return
     */
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
