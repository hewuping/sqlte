package hwp.sqlte;

/**
 * 实现类必须是线程安全的
 */
public interface Serializer {

    String encode(Object t);

    Object decode(String code);

/*    class JsonConverter implements Serializer {

        @Override
        public String toString(Map<String, Object> map) {
            return JSON.toJson(map)
        }

        @Override
        public Map<String, String> toObject(String code) {
            return JSON.fromJson(map)
        }

    }*/

}
