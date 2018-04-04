package hwp.sqlte.util;

import hwp.sqlte.example.User;

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

    private String name;
    private String desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void copyProperties(Object source, Object target) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
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

    public static void main(String[] args) throws Exception {
        copyProperties(new Beans(), new Beans());
    }

}
