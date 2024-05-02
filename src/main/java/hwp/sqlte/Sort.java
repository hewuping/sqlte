package hwp.sqlte;

import hwp.sqlte.Direction;
import hwp.sqlte.util.NameUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class Sort extends LinkedHashMap<String, Direction> implements Function<String, Direction> {

    /**
     * <pre>{@code
     * sql.orderBy(order -> {
     * 	order.by("name", sort.get("name")); // 如果为 null 则忽略
     * 	order.by("created_at", sort.getOrDefault("createdAt", Direction.ASC)); // 如果为 null 则使用 升序
     * }); }</pre>
     *
     * @param key the key whose associated value is to be returned
     * @return
     */
    @Override
    public Direction get(Object key) {
        return super.get(key);
    }

    /**
     * <pre>{@code
     * sql.orderBy(order -> {
     * 	order.by("name", sort.get("name")); // 如果为 null 则忽略
     * 	order.by("created_at", sort.getOrDefault("createdAt", Direction.ASC)); // 如果为 null 则使用 升序
     * }); }</pre>
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return
     */
    @Override
    public Direction getOrDefault(Object key, Direction def) {
        return super.getOrDefault(key, def);
    }

    /**
     * 同 {@code getOrDefault(key, Direction.ASC);}
     *
     * @param key
     * @return
     * @since 0.2.25
     */
    public Direction asc(Object key) {
        return getOrDefault(key, Direction.ASC);
    }

    /**
     * 同 {@code getOrDefault(key, Direction.DESC);}
     *
     * @param key
     * @return
     * @since 0.2.25
     */
    public Direction desc(Object key) {
        return getOrDefault(key, Direction.DESC);
    }

    /**
     * 根据表列名自动匹配排序规则
     *
     * @param column 表列名
     * @return
     * @since 0.2.25
     */
    @Override
    public Direction apply(String column) {
        return match(column);
    }

    /**
     * 根据表列名自动匹配排序规则 (表列名 -> 字段名), 适合单表或无列名冲突的多表
     *
     * <pre>{@code
     *  sql.orderBy(order -> {
     * 	order.by("user_id", sort::match);// 字段匹配, 默认为升序
     * 	order.by("created_at", sort::match);
     * }); }</pre>
     *
     * @param column 表列名
     * @return 如果未匹配到则返回 ASC
     * @since 0.2.24
     */
    public Direction match(String column) {
        // 首先精确查找
        Direction direction = get(column);
        if (direction != null) {
            return direction;
        }
        // 转为驼峰查找
        direction = get(NameUtils.toUpperCamel(column));
        if (direction != null) {
            return direction;
        }
        // 删除下划线且忽略大小写查找
        String column2 = column.replace("_", "");
        for (Map.Entry<String, Direction> entry : entrySet()) {
            if (column2.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        // 实在找不到了
        return null;
    }

}