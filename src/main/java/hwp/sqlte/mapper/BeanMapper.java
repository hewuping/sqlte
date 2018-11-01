package hwp.sqlte.mapper;


import hwp.sqlte.Helper;
import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;
import hwp.sqlte.UncheckedException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * @author Zero
 * Created on 2017/3/21.
 */
public class BeanMapper<T extends Object> implements RowMapper<T> {

    private Supplier<T> supplier;

    public BeanMapper(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T map(Row row) {
        return convert(row, supplier);
    }

    public static <T> T convert(Row from, Supplier<T> supplier) {
        try {
            T obj = supplier.get();
            Field[] fields = obj.getClass().getFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                        Object value = from.getValue(Helper.getColumnName(field));
                        if (value != null) {
                            field.set(obj, value);
                        }
                    }
                }
            }
            return obj;
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e);
        }
    }

}
