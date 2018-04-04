package hwp.sqlte.mapper;


import hwp.sqlte.MColumn;
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
public class PublicBeanMapper<T extends Object> implements RowMapper<T> {

    private Supplier<T> supplier;

    public PublicBeanMapper(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T map(Row row) {
        try {
            T obj = supplier.get();
            Field[] fields = obj.getClass().getFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                        MColumn column = field.getAnnotation(MColumn.class);
                        if (column == null) {
                            field.set(obj, row.getValue(field.getName()));
                        } else {
                            field.set(obj, row.getValue(column.column()));
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
