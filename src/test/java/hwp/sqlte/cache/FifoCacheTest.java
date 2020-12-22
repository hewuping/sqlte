/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package hwp.sqlte.cache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Zero
 * Created on 2020/12/22.
 */
public class FifoCacheTest {

    private final FifoCache<String> cache = new FifoCache<>(2);

    @Before
    public void init() {
        cache.put("a", "A");
        cache.put("b", "B");
    }

    @Test
    public void put() {
        cache.put("c", "C");
        Assert.assertNull(cache.get("a"));
        Assert.assertNotNull(cache.get("b"));
        Assert.assertNotNull(cache.get("c"));
    }

    @Test
    public void get() {
        Assert.assertNotNull(cache.get("a"));
    }

    @Test
    public void remove() {
        cache.remove("a");
        Assert.assertNull(cache.get("a"));
        Assert.assertNotNull(cache.get("b"));
    }

    @Test
    public void clear() {
        cache.clear();
        Assert.assertNull(cache.get("a"));
        Assert.assertNull(cache.get("b"));
    }
}