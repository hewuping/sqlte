package hwp.sqlte.util;

import hwp.sqlte.Direction;
import hwp.sqlte.Order;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Sort extends LinkedHashMap<String, Direction> {

    /**
     * 转换为 Order 对象
     * <p>
     * 多表查询时, 条件中的列名会包含表名/表别名, 开放给前端接口不一致, 这里可以做转换
     *
     * @param mapper
     * @return
     */
    public Order mapper(Consumer<Map<String, String>> mapper) {
        Map<String, String> map = new LinkedHashMap<>();
        mapper.accept(map);
        Order order = new Order();
        map.forEach((name, column) -> {
            Direction direction = get(name);
            if (direction != null) {
                order.by(column, direction);
            }
        });
        return order;
    }

    /**
     * 当参数名和列表一致时可使用该方法 (比如: user_id, 而不是 userId)
     *
     * @return
     * @deprecated 太隐晦了, 不利于后续项目维护, 不建议使用
     */
    @Deprecated
    public Order toOrder() {
        Order order = new Order();
        forEach((name, direction) -> {
            if (direction != null) {
                order.by(name, direction);
            }
        });
        return order;
    }

    /**
     * 适用于参数名和列名不一致情况, 或联表查询指定表名.
     * <p>
     * 当存在指定的排序名称时, 执行后续操作
     * <pre>{@code
     *  sql.orderBy(order -> {
     * 	sort.on("userId", direction -> order.by("user_id", direction));
     * 	sort.on("createdAt", direction -> order.by("created_at", direction));
     * 	sort.on("enabled", direction -> order.by("enabled", direction));
     * 	sort.on("state", direction -> order.by("state", direction));
     * });
     * } </pre>
     * <p>
     * 0.2.24+ 建议使用下面代码代替 {@code get() }或 {@code match() } 代替, 可以减少代码量
     * <pre>{@code
     * sql.orderBy(order -> {
     * 	order.by("user_id", sort.get("enabled")); // 如果为 null 则忽略
     * 	order.by("created_at", sort.getOrDefault("createdAt", Direction.ASC)); // 如果为 null 则使用 升序
     * }); }</pre>
     *
     * @param name     字段名 (一般是前端传递过来的参数名)
     * @param consumer
     */
    public void on(String name, Consumer<Direction> consumer) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(consumer);

        Direction direction = this.get(name);
        if (direction != null) {
            consumer.accept(direction);
        }
    }

    /**
     * 根据名称自动匹配排序 (表列名 -> 字段名), 适合单表或无列名冲突的多表
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
        return this.match(column, NameUtils::toUpperCamel, null);
    }

    /**
     * 根据名称自动匹配排序 (表列名 -> 字段名), 适合单表或无列名冲突的多表
     *
     * @param column 表列名
     * @param def    如果未匹配到的默认返回值
     * @return 返回 ASC/DESC
     * @since 0.2.24
     */
    public Direction match(String column, Direction def) {
        return this.match(column, NameUtils::toUpperCamel, def);
    }

    /**
     * 使用自定义方法根据名称匹配排序(表列名 -> 字段名), 适合单表或无列名冲突的多表
     *
     * @param column  表列名
     * @param matchFn 自定义匹配方法
     * @param def     如果未匹配到的默认返回值
     * @return 返回 ASC/DESC
     * @since 0.2.24
     */
    public Direction match(String column, Function<String, String> matchFn, Direction def) {
        Objects.requireNonNull(column, "column cannot be null");
        Objects.requireNonNull(matchFn, "matchFn cannot be null");
        Direction direction = match0(column, matchFn);
        if (direction != null) {
            return direction;
        }
        // user.username
        int i = column.lastIndexOf('.');
        if (i > 0) {
            column = column.substring(i + 1);
            direction = match0(column, matchFn);
        }
        return direction != null ? direction : def;
    }

    private Direction match0(String column, Function<String, String> matchFn) {
        Objects.requireNonNull(column, "column cannot be null");
        Objects.requireNonNull(matchFn, "matchFn cannot be null");
        // 先按规则匹配
        String maybeFieldName = matchFn.apply(column);
        Direction direction = get(maybeFieldName);
        if (direction != null) {
            return direction;
        }
        // 忽略大小写匹配
        for (Map.Entry<String, Direction> entry : entrySet()) {
            if (column.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }


}