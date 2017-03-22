package hwp.sqlte.mapper;


import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zero
 *         Created on 2017/3/21.
 */
public class BeanMapper<T extends Object> implements RowMapper<T> {

    private Class<T> clazz;

    private Map<String, FieldInfo[]> cache = new HashMap<>();

    public BeanMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T map(Row row) {
        try {
            FieldInfo[] fieldInfos = cache.get(clazz.getName());
            if (fieldInfos == null) {
                List<FieldInfo> list = new ArrayList<>();
                Method[] methods = clazz.getMethods();
                for (Field field : clazz.getFields()) {
                    String setter = "set" + Character.toUpperCase(field.getName().charAt(0));
                    if (field.getName().length() > 1) {
                        setter += field.getName().substring(1);
                    }
                    if (Modifier.isPublic(field.getModifiers())) {
                        FieldInfo fieldInfo = new FieldInfo(field, true);
                        list.add(fieldInfo);
                    } else {
                        for (Method method : methods) {
                            if (method.getParameters().length == 1 && method.getParameters()[0].getType() == field.getType()) {
                                if (method.getName().equals(field.getName()) || method.getName().equals(setter)) {
                                    FieldInfo fieldInfo = new FieldInfo(field, method);
                                    list.add(fieldInfo);
                                }
                            }
                        }
                    }
                }
                fieldInfos = list.toArray(new FieldInfo[list.size()]);
                cache.put(clazz.getName(), fieldInfos);
            }
            T obj = clazz.newInstance();
            for (FieldInfo fieldInfo : fieldInfos) {
                if (fieldInfo.isPublic) {
                    fieldInfo.field.set(obj, row.get(fieldInfo.field.getName()));
                } else if (fieldInfo.setter != null && Modifier.isPublic(fieldInfo.setter.getModifiers())) {
                    fieldInfo.setter.invoke(obj, row.get(fieldInfo.field.getName()));
                }
            }
            return obj;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    static class FieldInfo {
        Method setter;
        Field field;
        boolean isPublic;

        public FieldInfo(Field field, boolean isPublic) {
            this.field = field;
            this.isPublic = isPublic;
        }

        public FieldInfo(Field field, Method setMethod) {
            this.field = field;
            this.setter = setMethod;
        }
    }

}
