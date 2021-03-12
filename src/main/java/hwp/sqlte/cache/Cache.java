package hwp.sqlte.cache;

import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2019/9/6.
 */
public interface Cache<T> {

    void put(Object key, T value);

    T get(Object key);

    default T get(Object key, Supplier<T> supplier) {
        return get(key, supplier, true);
    }

    default T get(Object key, Supplier<T> supplier, boolean cache) {
        T o = get(key);
        if (o == null) {
            o = supplier.get();
            if (cache) {
                put(key, o);
            }
        }
        return o;
    }

    void remove(Object key);

    void clear();

}
