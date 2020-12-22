/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

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
