
package hwp.sqlte.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FifoCache<T> implements Cache<T> {

    private final LinkedHashMap<Object, T> map;
    private final Lock rlock;
    private final Lock wlock;

    public FifoCache(int cacheSize) {
        map = new LinkedHashMap<Object, T>(cacheSize, 0.75f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, T> eldest) {
                return size() > cacheSize;
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


}