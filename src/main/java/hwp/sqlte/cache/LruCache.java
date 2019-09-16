package hwp.sqlte.cache;

import java.util.LinkedHashMap;
import java.util.Map;

//https://github.com/mybatis/mybatis-3/tree/master/src/main/java/org/apache/ibatis/cache/decorators
public class LruCache implements Cache {

    private Map<Object, Object> cache;

    public LruCache(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize < 1");
        }
        cache = new LinkedHashMap<Object, Object>(maxSize, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                return size() > maxSize;
            }
        };
    }

    @Override
    public synchronized void put(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return cache.get(key);
    }

    @Override
    public synchronized void remove(Object key) {
        cache.remove(key);
    }

    @Override
    public synchronized void clear() {
        cache.clear();
    }

/*    public static void main(String[] args) {
        LruCache cache = new LruCache(3);
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        System.out.println(cache.get("a"));
        cache.put("d", "D");
        System.out.println(cache.get("b"));
    }*/
}