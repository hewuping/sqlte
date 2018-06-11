package hwp.sqlte.mapper;


import hwp.sqlte.Column;
import hwp.sqlte.Row;
import hwp.sqlte.RowMapper;
import hwp.sqlte.UncheckedException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * @author Zero
 *         Created on 2017/3/21.
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
                        Column column = field.getAnnotation(Column.class);
                        if (column == null) {
                            field.set(obj, from.getValue(field.getName()));
                        } else {
                            field.set(obj, from.getValue(column.column()));
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
