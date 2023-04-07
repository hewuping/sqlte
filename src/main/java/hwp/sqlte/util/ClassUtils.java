package hwp.sqlte.util;

import java.lang.reflect.InvocationTargetException;

public class ClassUtils {

    public static  <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
