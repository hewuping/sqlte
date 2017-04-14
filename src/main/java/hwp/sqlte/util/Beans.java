package hwp.sqlte.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Zero
 *         Created on 2017/3/24.
 */
public class Beans {

    public static void copyProperties(Object source, Object target) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
        System.out.println(beanInfo);
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod != null) {
                Method readMethod = descriptor.getReadMethod();
                Object value = readMethod.invoke(source);
                writeMethod.invoke(target, value);
            }
            System.out.println(descriptor.getName());
            System.out.println(descriptor.getReadMethod());
            System.out.println(descriptor.getWriteMethod());
        }
    }

}
