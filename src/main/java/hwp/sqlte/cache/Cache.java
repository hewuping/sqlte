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
public interface Cache {

    void put(Object key, Object value);

    Object get(Object key);

    default Object get(String key, Supplier<Object> supplier) {
        Object o = get(key);
        if (o == null) {
            o = supplier.get();
            put(key, o);
        }
        return o;
    }

    void remove(Object key);

    void clear();

}
