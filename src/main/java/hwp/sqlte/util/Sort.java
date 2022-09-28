package hwp.sqlte.util;

import hwp.sqlte.Direction;
import hwp.sqlte.Order;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class Sort extends LinkedHashMap<String, Direction> {


    /**
     * 转换为 Order 对象
     * <p>
     * 多表查询时, 条件中的列名会包含表名/表别名, 开放给前端接口不一致, 这里可以做转换
     *
     * @param mapper
     * @return
     */
    public Order asOrder(Consumer<Map<String, String>> mapper) {
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
     * 当存在指定的排序名称时, 执行后续操作
     *
     * @param name
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

    {
        asOrder(mapper -> {
            mapper.put("name", "user.name");
            mapper.put("group", "group.name");
        });
    }

}