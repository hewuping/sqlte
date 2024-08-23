package hwp.sqlte;

import java.util.function.Consumer;

/**
 * 数据加载器
 *
 * @param <T>
 */
public interface DataLoader<T> {

    /**
     * 加载数据
     *
     * @param consumer 数据消费着
     */
    void load(Consumer<T> consumer);

}
