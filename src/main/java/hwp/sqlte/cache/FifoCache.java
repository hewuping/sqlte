
package hwp.sqlte.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class FifoCache implements Cache {

    private LinkedHashMap<Object, Object> map;

    public FifoCache(int cacheSize) {
        map = new LinkedHashMap<Object, Object>(cacheSize, 0.75f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                return size() > cacheSize;
            }
        };
    }

    @Override
    public synchronized void put(Object key, Object value) {
        map.put(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return map.get(key);
    }

    @Override
    public synchronized void remove(Object key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }


}