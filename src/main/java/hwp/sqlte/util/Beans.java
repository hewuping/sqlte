package hwp.sqlte.util;

import hwp.sqlte.example.User;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author Zero
 * Created on 2017/3/24.
 */
public class Beans extends User {

    private String name;
    public String desc;
    public boolean goOd;

    public String getName0() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isGood() {
        return goOd;
    }

    public void setGood(boolean good) {
        this.goOd = good;
    }

    public String getXXX() {
        return "";
    }

    public static void copyProperties(Object source, Object target) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass(), Introspector.IGNORE_ALL_BEANINFO);
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod != null) {
                Method readMethod = descriptor.getReadMethod();
//                Object value = readMethod.invoke(source);
//                writeMethod.invoke(target, value);
            }
            //要么是Public的Field, 要么同时拥有Getter和Setter
            System.out.println("Field: " + descriptor.getName());
            System.out.println("Getter: " + descriptor.getReadMethod());
            System.out.println("Setter: " + descriptor.getWriteMethod());
        }
    }

    public static void main(String[] args) throws Exception {
        copyProperties(new Beans(), new Beans());
    }

}
