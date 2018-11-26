package hwp.sqlte;

import java.util.Map;

/**
 * @author Zero
 * Created on 2018/11/20.
 */
public interface Serializer<T> {

    String encode(T t);

    T decode(String code);

    default boolean isThreadSafe() {
        return false;
    }

    class JsonTranscoder implements Serializer<Map<String, Object>> {

        @Override
        public String encode(Map<String, Object> map) {
            //return JSON.toJson(map)
            return null;
        }

        @Override
        public Map<String, Object> decode(String code) {
            //return JSON.fromJson(map)
            return null;
        }
    }

}
