package hwp.sqlte;

/**
 * @author Zero
 * Created on 2018/11/20.
 */
public interface Converter<T> {

    String toString(T t);

    T toObject(String code);

    default boolean isThreadSafe() {
        return false;
    }

/*    class JsonExampleConverter implements Converter<Map<String, String>> {

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
