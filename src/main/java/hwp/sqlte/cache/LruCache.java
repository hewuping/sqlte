package hwp.sqlte.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//https://github.com/mybatis/mybatis-3/tree/master/src/main/java/org/apache/ibatis/cache/decorators
public class LruCache<T> implements Cache<T> {

    private final Map<Object, T> map;
    private final Lock rlock;
    private final Lock wlock;

    public LruCache(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize < 1");
        }
        map = new LinkedHashMap<Object, T>(maxSize, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, T> eldest) {
                return size() > maxSize;
            }
        };
        ReadWriteLock lock = new ReentrantReadWriteLock();
        rlock = lock.readLock();
        wlock = lock.writeLock();
    }

    @Override
    public void put(Object key, T value) {
        wlock.lock();
        try {
            map.put(key, value);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public T get(Object key) {
        rlock.lock();
        try {
            return map.get(key);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public void remove(Object key) {
        wlock.lock();
        try {
            map.remove(key);
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public void clear() {
        wlock.lock();
        try {
            map.clear();
        } finally {
            wlock.unlock();
        }
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